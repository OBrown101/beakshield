package com.beakshield.dawson

import com.beakshield.user.User
import com.beakshield.websocket.AgentData
import com.beakshield.websocket.ChatData
import com.beakshield.websocket.ChatData.DataType
import com.beakshield.websocket.ConfigData
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
import com.beakshield.websocket.WSPacket.PacketType.CONFIG_DATA
import com.beakshield.websocket.WSPacket.PacketType.USER_INPUT_REQUEST_RESPONSE
import com.beakshield.websocket.WSPacket.PacketType.ERROR
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Dawson {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val socket = WebSocketClient()
    val connectionState = socket.connectionState
    private var syncTimerJob: Job? = null

    private val _currentUserUUID = MutableStateFlow<String?>(null)
    val currentUserUUID = _currentUserUUID.asStateFlow()

    private val _activeAgents = MutableStateFlow<List<Agent>>(emptyList())
    val activeAgents = _activeAgents.asStateFlow()

    private val _activeUsers = MutableStateFlow<List<User>>(emptyList())
    val users = _activeUsers.asStateFlow()

    private val _activeChats = MutableStateFlow<List<Chat>>(emptyList())
    val activeChats = _activeChats.asStateFlow()

    private val _pendingInputRequests = MutableStateFlow<List<UserInputRequest>>(emptyList())
    val pendingInputRequests = _pendingInputRequests.asStateFlow()

    init {
        _activeUsers.update { it + User.defaultUser }          // USED FOR TESTING (NOT PRODUCTION)
        _currentUserUUID.value = User.defaultUser.uuid      // USED FOR TESTING (NOT PRODUCTION)

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
                    CONFIG_DATA -> {
                        packet.payloadAs<ConfigData>()?.let {
                            handleConfigData(it)
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

    @OptIn(ExperimentalUuidApi::class)
    fun handleAgentData(data: AgentData) {
        when (data.dataType) {
            AgentData.DataType.TEXT_THINKING -> {
                val text = data.payloadAs<String>() ?: return
                val msgType = Message.MsgType.TEXT_THINKING
                val newMessage = Message(
                    uuid = msgType.getStreamUUID(data.dataUUID),
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = msgType,
                    chunks = mutableMapOf(0 to text),
                    isStream = true
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage, data.dataIndex)
            }
            AgentData.DataType.TEXT_RESPONSE -> {
                val text = data.payloadAs<String>() ?: return
                val msgType = Message.MsgType.TEXT_RESPONSE
                val newMessage = Message(
                    uuid = msgType.getStreamUUID(data.dataUUID),
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = msgType,
                    chunks = mutableMapOf(0 to text),
                    isStream = true
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage, data.dataIndex)
            }
            AgentData.DataType.DATA_RESPONSE -> {}
            AgentData.DataType.TOOL_CALL -> {
                val text = data.payloadAs<String>() ?: return
                val msgType = Message.MsgType.TOOL_CALL_NAME
                val newMessage = Message(
                    uuid = msgType.getStreamUUID(data.dataUUID),
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = msgType,
                    chunks = mutableMapOf(0 to "\n## TOOL CALLED: $text ##\n"),
                    isStream = true
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage, data.dataIndex)
            }
            AgentData.DataType.TOOL_RESULT -> {
                val text = data.payloadAs<String>() ?: return
                val msgType = Message.MsgType.TOOL_CALL_RESULT
                val newMessage = Message(
                    uuid = msgType.getStreamUUID(data.dataUUID),
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = msgType,
                    chunks = mutableMapOf(0 to "\n## TOOL RESULT: ${text.take(20)} ##\n"),
                    isStream = true
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage, data.dataIndex)
            }
            AgentData.DataType.USER_INPUT_REQUEST -> {
                val request = data.payloadAs<UserInputRequest>() ?: return
                _pendingInputRequests.update { it + request }
            }
            AgentData.DataType.DATA_LAST_INDEX -> {
                val lastIndex = data.payloadAs<Int>()
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.markMessageComplete(data.dataUUID, lastIndex)
            }
            AgentData.DataType.ERROR -> {}
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

    fun handleConfigData(data: ConfigData) {
        println("handleConfigData: ${data.dataType}")
        when (data.dataType) {
            ConfigData.DataType.UPSERT_AGENT -> {
                val agent = data.payloadAs<Agent>() ?: return
                upsertAgent(agent)
            }
            ConfigData.DataType.DELETE_AGENT -> {
                val agentUUID = data.payloadAs<String>() ?: return
                deleteAgent(agentUUID)
            }
            ConfigData.DataType.UPSERT_USER -> {
                val user = data.payloadAs<User>() ?: return
                upsertUser(user)
            }
            ConfigData.DataType.DELETE_USER -> {
                val userUUID = data.payloadAs<String>() ?: return
                deleteUser(userUUID)
            }
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

    private fun upsertAgent(agent: Agent) {
        if (_activeAgents.value.none { it.uuid == agent.uuid }) {
            addAgent(agent)
        } else {
            val currentTimestamp = _activeAgents.value.firstOrNull { it.uuid == agent.uuid }?.updatedTimestamp ?: return
            if (currentTimestamp >= agent.updatedTimestamp) return

            _activeAgents.value.firstOrNull { it.uuid == agent.uuid }?.updatedTimestamp = agent.updatedTimestamp
            // Any agent specific settings/data will be updated here
        }
    }

    private fun deleteAgent(agentUUID: String) {
        _activeAgents.update { agents ->
            agents.filterNot { it.uuid == agentUUID }
        }
        println("Agent (${agentUUID}) deleted.")
    }

    private fun addAgent(agent: Agent) {
        _activeAgents.update { it + agent }
        println("Agent (${agent.uuid}) added.")
    }

    private fun upsertUser(user: User) {
        if (_activeUsers.value.none { it.uuid == user.uuid }) {
            addUser(user)
        } else {
            val currentTimestamp = _activeUsers.value.firstOrNull { it.uuid == user.uuid }?.updatedTimestamp ?: return
            if (currentTimestamp >= user.updatedTimestamp) return

            _activeUsers.value.firstOrNull { it.uuid == user.uuid }?.updatedTimestamp = user.updatedTimestamp
            // Any user specific settings/data will be updated here
        }
    }

    private fun deleteUser(userUUID: String) {
        _activeUsers.update { users ->
            users.filterNot { it.uuid == userUUID }
        }
        _activeChats.value.filter { it.userUUID == userUUID }.forEach { chat ->
            deleteChat(chat.uuid)
        }
        println("User (${userUUID}) deleted.")
    }

    private fun addUser(user: User) {
        _activeUsers.update { it + user }
        println("User (${user.uuid}) added.")
    }

    private fun upsertChat(chat: Chat) {
        if (_activeChats.value.none { it.uuid == chat.uuid }) {
            addChat(chat)
        } else {
            val currentTimestamp = _activeChats.value.firstOrNull { it.uuid == chat.uuid }?.updatedTimestamp ?: return
            if (currentTimestamp >= chat.updatedTimestamp) return

            _activeChats.value.firstOrNull { it.uuid == chat.uuid }?.updatedTimestamp = chat.updatedTimestamp
            // Any chat specific settings/data will be updated here
        }
    }

    private fun deleteChat(chatUUID: String) {
        val agentUUID = _activeChats.value.firstOrNull { it.uuid == chatUUID }?.agentUUID
        _activeAgents.value.firstOrNull { it.uuid == agentUUID }?.let { agent ->
            deleteAgent(agent.uuid)
        }
        _activeChats.update { chats ->
            chats.filterNot { it.uuid == chatUUID }
        }
        println("Chat (${chatUUID}) deleted.")
    }

    private fun addChat(chat: Chat) {
        _activeChats.update { it + chat }
        println("Chat (${chat.uuid}) added.")
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

    companion object {
        const val PRIMARY_CHAT_UUID = "PRIMARY_CHAT"
        const val PRIMARY_AGENT_UUID = "PRIMARY_AGENT"
    }
}