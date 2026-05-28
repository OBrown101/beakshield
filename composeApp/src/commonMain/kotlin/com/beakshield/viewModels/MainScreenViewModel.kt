package com.beakshield.viewModels

import com.beakshield.BeakShieldApp.Companion.dawson
import com.beakshield.dawson.Agent
import com.beakshield.dawson.Dawson
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

    private val _userSelected = MutableStateFlow<User?>(null)
    private val _allMessages = MutableStateFlow<List<Message>>(emptyList())
    val userSelected = _userSelected.asStateFlow()
    val allMessages = _allMessages.asStateFlow()

    val pendingInputRequests = dawson.pendingInputRequests

    val groupedMessages: StateFlow<Map<String, List<Message>>> =
        allMessages.map { allMsgs ->
            allMsgs.groupBy { it.dataUUID }
                .mapValues { (_, segs) ->
                    segs.sortedBy { it.timestamp }
                }
        }.stateIn(scope, SharingStarted.Lazily, emptyMap())

    init {
        _userSelected.value = User(uuid = User.DEFAULT_USER_UUID, "Owen")

        scope.launch {
            dawson.activeChats.flatMapLatest { chats ->
                if (chats.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(chats.map { it.messages }) { messages ->
                        messages.flatMap { it }
                    }
                }
            }.collect { allMessages ->
                _allMessages.value = allMessages
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun sendTextPrompt(text: String) {
        val user = _userSelected.value ?: return
        if (dawson.activeChats.value.isEmpty()) {
            dawson.startNewChat()
            return
        }
        val chatUUID = dawson.activeChats.value.firstOrNull()?.uuid ?: return   // For testing
        val message = Message(sourceUUID = user.uuid, destinationUUID = Agent.PRIMARY_UUID, type = Message.MsgType.TEXT_PROMPT, text = text)
        dawson.sendMessage(message, chatUUID)
    }

    fun setIPAddress(ipAddress: String) {
        dawson.connect(ipAddress)
    }

    fun respondToRequest(request: UserInputRequest, approved: Boolean?, response: String? = null) {
        dawson.respondToRequest(request, approved, response)
    }
}