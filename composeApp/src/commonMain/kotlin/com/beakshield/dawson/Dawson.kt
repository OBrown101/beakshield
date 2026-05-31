package com.beakshield.dawson

import com.beakshield.user.User
import com.beakshield.websocket.AgentData
import com.beakshield.websocket.ChatData
import com.beakshield.websocket.ChatData.DataType
import com.beakshield.websocket.MessageData
import com.beakshield.websocket.UserData
import com.beakshield.websocket.UserInputRequest
import com.beakshield.websocket.UserInputResponse
import com.beakshield.websocket.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.beakshield.websocket.WSPacket.PacketType.PONG
import com.beakshield.websocket.WSPacket.PacketType.AGENT_DATA
import com.beakshield.websocket.WSPacket.PacketType.CHAT_DATA
import com.beakshield.websocket.WSPacket.PacketType.USER_DATA
import com.beakshield.websocket.WSPacket.PacketType.USER_INPUT_REQUEST_RESPONSE
import com.beakshield.websocket.WSPacket.PacketType.ERROR
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Dawson {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val socket = WebSocketClient()
    val connectionState = socket.connectionState
    private var syncTimerJob: Job? = null

    private val _users = MutableStateFlow<List<User>>(emptyList())
    private val _currentUserUUID = MutableStateFlow<String?>(null)
    val users = _users.asStateFlow()
    val currentUserUUID = _currentUserUUID.asStateFlow()

//    private val _agents = MutableStateFlow<List<Agent>>(emptyList())
//    val agents = _agents.asStateFlow()

    private val _activeChats = MutableStateFlow<List<Chat>>(emptyList())
    val activeChats = _activeChats.asStateFlow()

    private val _pendingInputRequests = MutableStateFlow<List<UserInputRequest>>(emptyList())
    val pendingInputRequests = _pendingInputRequests.asStateFlow()

    init {
        _users.update { it + User.defaultUser }          // USED FOR TESTING (NOT PRODUCTION)
        _currentUserUUID.value = User.defaultUser.uuid   // USED FOR TESTING (NOT PRODUCTION)

        scope.launch {
            socket.incomingPackets.collect { packet ->
                println("Packet received from DAWSON server: TYPE: ${packet.type}, PAYLOAD: ${packet.payload}")
                when (packet.type) {
                    PONG -> {
                        println("Server pong")
                    }
                    AGENT_DATA -> {
                        packet.payloadAs<AgentData>()?.let {
                            handleAgentData(it)
                        }
                    }
                    CHAT_DATA -> {
                        packet.payloadAs<ChatData>()?.let {
                            handleChatData(it)
                        }
                    }
                    ERROR -> {}
                    else -> {}
                }
            }
        }
        scope.launch {
            var prevConn = false
            connectionState.collect { connected ->
                if (!prevConn && connected) {
                    startSyncTimer()
                }
                prevConn = connected
            }
        }
    }

    fun connect(ipAddress: String) {
        socket.connect(ipAddress)
    }

    fun disconnect() {
        socket.disconnect()
    }

    private fun startSyncTimer() {
        syncTimerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                println("Requesting sync of all chats/chat-messages")
                fetchChats()
                fetchChatMessages()
                delay(10000L)
            }
        }
    }

    fun handleChatData(data: ChatData) {
        println("handleChatData: ${data.dataType}")
        when (data.dataType) {
            DataType.UPSERT_CHAT -> {
                val chat = data.payloadAs<Chat>() ?: return
                upsertChat(chat)
            }
            DataType.DELETE_CHAT -> {
                val chatUUID = data.chatUUID ?: return
                deleteChat(chatUUID)
            }
            DataType.SYNC_CHAT -> {
                data.chatUUID?.let {
                    val chat = data.payloadAs<Chat>() ?: return
                    upsertChat(chat)
                } ?: run {
                    val chats = data.payloadAs<List<Chat>>() ?: return
                    chats.forEach {
                        upsertChat(it)
                    }
                }
            }
            DataType.SYNC_CHAT_MESSAGES -> {
                val messageDatas = data.payloadAs<List<MessageData>>() ?: return
                data.chatUUID?.let { chatUUID ->
                    val messages = messageDatas.map { Message(it) }
                    _activeChats.value.firstOrNull { it.uuid == chatUUID }?.syncMessages(messages)
                } ?: run {
                    messageDatas.groupBy { it.chatUUID }.forEach { (chatUUID, msgDatas) ->
                        val messages = msgDatas.map { Message(it) }
                        _activeChats.value.firstOrNull { it.uuid == chatUUID }?.syncMessages(messages)
                    }
                }
            }
        }
    }

    fun handleAgentData(data: AgentData) {
        when (data.dataType) {
            AgentData.DataType.TEXT_THINKING -> {
                val text = data.payloadAs<String>() ?: return
                val newMessage = Message(
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = Message.MsgType.TEXT_THINKING,
                    chunks = mutableMapOf(0 to text)
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage, data.dataIndex)
            }
            AgentData.DataType.TEXT_RESPONSE -> {
                val text = data.payloadAs<String>() ?: return
                val newMessage = Message(
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = Message.MsgType.TEXT_RESPONSE,
                    chunks = mutableMapOf(0 to text)
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage, data.dataIndex)
            }
            AgentData.DataType.DATA_RESPONSE -> {}
            AgentData.DataType.TOOL_CALL -> {
                val text = data.payloadAs<String>() ?: return
                val newMessage = Message(
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = Message.MsgType.TOOL_CALL_NAME,
                    chunks = mutableMapOf(0 to "\n## TOOL CALLED: $text ##\n")
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage, data.dataIndex)
            }
            AgentData.DataType.TOOL_RESULT -> {
                val text = data.payloadAs<String>() ?: return

                val newMessage = Message(
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = Message.MsgType.TOOL_CALL_RESULT,
                    chunks = mutableMapOf(0 to "\n## TOOL RESULT: ${text.take(20)} ##\n")
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage, data.dataIndex)
            }
            AgentData.DataType.USER_INPUT_REQUEST -> {
                val request = data.payloadAs<UserInputRequest>() ?: return
                _pendingInputRequests.update { it + request }
            }
            AgentData.DataType.ERROR -> {}
        }
    }

    fun respondToRequest(request: UserInputRequest, approved: Boolean?, response: String? = null) {
        _pendingInputRequests.update { requests ->
            requests.filterNot { it.agentUUID == request.agentUUID }
        }

        val payload = UserInputResponse(request.agentUUID, request.userUUID, approved, response)
        socket.send(payload, UserInputResponse::class, USER_INPUT_REQUEST_RESPONSE)
    }

    fun getAgentUUIDForChat(chatUUID: String): String? {
        return _activeChats.value.firstOrNull { it.uuid == chatUUID }?.agentUUID
    }

    fun startPrimaryChat(): Chat? {
        val userUUID = _currentUserUUID.value ?: return null
        if (_activeChats.value.any { (it.uuid == PRIMARY_CHAT_UUID) && it.userUUID == userUUID }) return null

        val newChat = Chat(PRIMARY_CHAT_UUID, userUUID, PRIMARY_AGENT_UUID)
        val chatData = ChatData(
            chatUUID = PRIMARY_CHAT_UUID,
            userUUID = userUUID,
            agentUUID = PRIMARY_AGENT_UUID,
            dataType = DataType.UPSERT_CHAT,
            payload = WebSocketClient.json.encodeToJsonElement(serializer<Chat>(), newChat)
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
        return newChat
    }

    @OptIn(ExperimentalUuidApi::class)
    fun startSquireChat(chatUUID: String = Uuid.random().toString()): Chat? {
        val userUUID = _currentUserUUID.value ?: return null
        val agentUUID = Uuid.random().toString()
        if (_activeChats.value.any { it.userUUID == userUUID && it.agentUUID == agentUUID }) return null

        val newChat = Chat(chatUUID, userUUID, agentUUID)
        val chatData = ChatData(
            chatUUID = newChat.uuid,
            userUUID = userUUID,
            agentUUID = agentUUID,
            dataType = DataType.UPSERT_CHAT,
            payload = WebSocketClient.json.encodeToJsonElement(serializer<Chat>(), newChat)
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
        return newChat
    }

    fun sendMessage(message: Message, chatUUID: String, dataIndex: Int = 0) {
        _activeChats.value.firstOrNull { it.uuid == chatUUID }?.addPendingMessage(message, dataIndex)
        val userData = UserData(message, chatUUID)
        socket.send(userData, UserData::class, USER_DATA)
    }

    fun upsertChat(chat: Chat) {
        if (_activeChats.value.none { it.uuid == chat.uuid }) {
            addChat(chat)
        } else {
            val currentTimestamp = _activeChats.value.firstOrNull { it.uuid == chat.uuid }?.updatedTimestamp ?: return
            if (currentTimestamp >= chat.updatedTimestamp) return
            // Any chat specific settings/data will be updated here
        }
    }

    fun fetchChats() {
        val userUUID = _currentUserUUID.value ?: return
        val chatData = ChatData(
            chatUUID = null,
            userUUID = userUUID,
            agentUUID = null,
            dataType = DataType.SYNC_CHAT,
            payload = JsonNull
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    fun fetchChatMessages() {
        val userUUID = _currentUserUUID.value ?: return
        val chatData = ChatData(
            chatUUID = null,
            userUUID = userUUID,
            agentUUID = null,
            dataType = DataType.SYNC_CHAT_MESSAGES,
            payload = JsonNull
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    fun deleteChat(chatUUID: String) {
        _activeChats.update { chats ->
            chats.filterNot { it.uuid == chatUUID }
        }
        println("Chat (${chatUUID}) deleted.")
    }

    private fun addChat(chat: Chat) {
        _activeChats.update { it + chat }
        println("Chat (${chat.uuid}) added.")
    }

    companion object {
        const val PRIMARY_CHAT_UUID = "PRIMARY_CHAT"
        const val PRIMARY_AGENT_UUID = "PRIMARY_AGENT"
    }
}