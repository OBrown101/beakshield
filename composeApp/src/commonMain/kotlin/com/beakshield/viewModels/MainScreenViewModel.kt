package com.beakshield.viewModels

import com.beakshield.BeakShieldApp.Companion.dawson
import com.beakshield.dawson.Message
import com.beakshield.websocket.UserInputRequest
import com.beakshield.user.User
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
class MainScreenViewModel {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _chatUUIDSelected = MutableStateFlow<String?>(null)
    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatUUIDSelected = _chatUUIDSelected.asStateFlow()
    val allMessages = _chatMessages.asStateFlow()

    val pendingInputRequests = dawson.pendingInputRequests

    val groupedMessages: StateFlow<Map<String, List<Message>>> =
        allMessages.map { allMsgs ->
            allMsgs.groupBy { it.groupingKey }.mapValues { (_, segs) ->
                segs.sortedBy { it.updatedTimestamp }
            }
        }.stateIn(scope, SharingStarted.Lazily, emptyMap())

    init {
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

    fun selectChat(chatUUID: String?) {
        _chatUUIDSelected.value = chatUUID
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
        val message = Message(
            dataUUID = Uuid.random().toString(),
            sourceUUID = userUUID,
            destinationUUID = agentUUID,
            type = Message.MsgType.TEXT_PROMPT,
            chunks = mutableMapOf(0 to text)
        )
        dawson.sendMessage(message, chatUUID)
    }

    fun setIPAddress(ipAddress: String) {
        dawson.connect(ipAddress)
    }
}