package com.beakshield.viewModels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.beakshield.BeakShieldApp.baseScreenViewModel
import com.beakshield.BeakShieldApp.dawson
import com.beakshield.dawson.Agent
import com.beakshield.dawson.Chat
import com.beakshield.dawson.LLMModel
import com.beakshield.dawson.Message
import com.beakshield.screens.Destination
import com.beakshield.screens.chatsScreen.ChatsSideRail
import com.beakshield.tablecells.ChatCellViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ChatsScreenViewModel : VModel {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _railContent = MutableStateFlow<RailContent?>(null)
    override val railContent = _railContent.asStateFlow()

    private val _chatUUIDSelected = MutableStateFlow<String?>(null)
    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatUUIDSelected = _chatUUIDSelected.asStateFlow()
    val allMessages = _chatMessages.asStateFlow()

    val currentAgent: StateFlow<Agent?> =
        combine(_chatUUIDSelected, dawson.activeChats, dawson.activeAgents) { chatUUID, chats, agents ->
            val agentUUID = chats.firstOrNull { it.uuid == chatUUID }?.agentUUID
            agents.firstOrNull { it.uuid == agentUUID }
        }.stateIn(scope, SharingStarted.Eagerly, null)

    val currentTitle: StateFlow<String?> = combine(_chatUUIDSelected, dawson.activeChats) { chatUUID, chats ->
        chats.firstOrNull { it.uuid == chatUUID }?.title
    }.stateIn(scope, SharingStarted.Lazily, null)

    val currentSubtitle: StateFlow<String?> = combine(_chatUUIDSelected, dawson.activeChats) { chatUUID, chats ->
        chats.firstOrNull { it.uuid == chatUUID }?.subtitle
    }.stateIn(scope, SharingStarted.Lazily, null)

    val groupedMessages: StateFlow<Map<String, List<Message>>> =
        allMessages.map { allMsgs ->
            allMsgs.groupBy { msg ->
                if (msg.isStream) "${msg.dataUUID}_${msg.sourceUUID}" else msg.uuid
            }.mapValues { (_, segs) ->
                segs.sortedBy { it.createdTimestamp }
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    private val _searchText = MutableStateFlow("")

    val chatCellViewModels: StateFlow<List<ChatCellViewModel>> =
        combine(dawson.activeChats, _searchText, _chatUUIDSelected) { chats, searchText, selectedUUID ->
            buildChatCellViewModels(chats, searchText, selectedUUID)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        setRailContent(width = 340) { modifier ->
            val chatCellViewModels = chatCellViewModels.collectAsState()
            ChatsSideRail(
                modifier = modifier,
                chatCellViewModels = chatCellViewModels.value,
                onSearchChanged = {
                    _searchText.value = it
                },
                onBack = { baseScreenViewModel.navToScreen(Destination.MAIN) },
                onNewChat = {
                    startNewChat()
                }
            )
        }

        scope.launch {
            dawson.activeChats.collect { chats ->
                val selectedUUID = _chatUUIDSelected.value
                val selectedStillExists = chats.any { it.uuid == selectedUUID }

                if ((selectedUUID == null) || !selectedStillExists) {
                    selectChat(chats.maxByOrNull { it.updatedTimestamp }?.uuid)
                }
            }
        }

        scope.launch {
            _chatUUIDSelected.flatMapLatest { chatUUID ->
                if (chatUUID == null) {
                    flowOf(emptyList())
                } else {
                    dawson.activeChats.flatMapLatest { chats ->
                        chats.find { it.uuid == chatUUID }?.messages ?: flowOf(emptyList())
                    }
                }
            }.collect { messages ->
                _chatMessages.value = messages
            }
        }
    }

    private fun buildChatCellViewModels(chats: List<Chat>, searchText: String, selectedUUID: String?): List<ChatCellViewModel> {
        val query = searchText.trim()
        return chats
            .filter { chat ->
                query.isBlank() ||
                    chat.title.contains(query, ignoreCase = true) ||
                    chat.subtitle.contains(query, ignoreCase = true)
            }
            .sortedByDescending { it.updatedTimestamp }
            .mapIndexed { index, chat ->
                getChatCellViewModel(index, chat, selectedUUID)
            }
    }

    private fun getChatCellViewModel(index: Int, chat: Chat, selectedUUID: String?): ChatCellViewModel {
        return ChatCellViewModel(
            id = index.toLong(),
            chat = chat,
            onSelect = {
                selectChat(chat.uuid)
            },
            onDelete = {
                dawson.deleteChat(chat)
            }
        ).apply {
            selected = chat.uuid == selectedUUID
        }
    }

    fun setRailContent(width: Int, content: (@Composable (Modifier) -> Unit)?) {
        _railContent.value = RailContent(content, width)
    }

    fun setTitle(title: String) {
        val chatUUID = _chatUUIDSelected.value ?: return
        val updatedChat = dawson.activeChats.value.firstOrNull { it.uuid == chatUUID }?.copy(
            title = title
        ) ?: return
        dawson.updateChat(updatedChat)
    }

    fun setMode(mode: Agent.Mode) {
        val chatUUID = _chatUUIDSelected.value ?: return
        val agentUUID = dawson.activeChats.value.firstOrNull { it.uuid == chatUUID }?.agentUUID ?: return
        val updatedAgent = dawson.activeAgents.value.firstOrNull { it.uuid == agentUUID }?.copy(
            mode = mode
        ) ?: return
        dawson.updateAgent(updatedAgent)
    }

    fun setModel(llmModel: LLMModel) {
        val chatUUID = _chatUUIDSelected.value ?: return
        val agentUUID = dawson.activeChats.value.firstOrNull { it.uuid == chatUUID }?.agentUUID ?: return
        val updatedAgent = dawson.activeAgents.value.firstOrNull { it.uuid == agentUUID }?.copy(
            model = llmModel
        ) ?: return
        dawson.updateAgent(updatedAgent)
    }

    fun addDirectories(directories: String) {
        val chatUUID = _chatUUIDSelected.value ?: return
        val agentUUID = dawson.activeChats.value.firstOrNull { it.uuid == chatUUID }?.agentUUID ?: return
        val curAgent = dawson.activeAgents.value.firstOrNull { it.uuid == agentUUID }
        val updatedAgent = curAgent?.copy(
            directories = (curAgent.directories + directories).distinct()
        ) ?: return
        dawson.updateAgent(updatedAgent)
    }

    fun selectChat(chatUUID: String?) {
        _chatUUIDSelected.value = chatUUID
        chatUUID?.let {
            dawson.fetchChatMessages(it)
        }
    }

    fun startPrimaryChat() {
        dawson.startPrimaryChat()?.let {
            selectChat(it.uuid)
        }
    }

    fun startNewChat() {
        dawson.startSquireChat()?.let {
            selectChat(it.uuid)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun sendTextPrompt(text: String) {
        val userUUID = dawson.currentUserUUID.value ?: return
        val chatUUID = _chatUUIDSelected.value ?: return
        val agentUUID = dawson.getAgentUUIDForChat(chatUUID) ?: return
        val dataUUID = Uuid.random().toString()
        val msgType = Message.MsgType.TEXT_PROMPT
        val message = Message(
            uuid = msgType.getStreamUUID(dataUUID),
            dataUUID = dataUUID,
            sourceUUID = userUUID,
            destinationUUID = agentUUID,
            type = msgType,
            chunks = mutableMapOf(0 to text),
            delivered = false,
            isStream = true
        )
        dawson.sendMessage(message, chatUUID)
    }
}