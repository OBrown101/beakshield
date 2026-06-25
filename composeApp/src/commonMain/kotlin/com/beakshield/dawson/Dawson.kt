package com.beakshield.dawson

import com.beakshield.BeakShieldApp.preferences
import com.beakshield.websocket.AgentData
import com.beakshield.websocket.ChatData
import com.beakshield.websocket.ChatData.DataType
import com.beakshield.websocket.ConfigData
import com.beakshield.websocket.MessageData
import com.beakshield.websocket.ServerConnState
import com.beakshield.websocket.SyncState
import com.beakshield.websocket.UserData
import com.beakshield.websocket.UserInputRequest
import com.beakshield.websocket.UserInputResponse
import com.beakshield.websocket.WSPacket.PacketType.AGENT_DATA
import com.beakshield.websocket.WSPacket.PacketType.CHAT_DATA
import com.beakshield.websocket.WSPacket.PacketType.CONFIG_DATA
import com.beakshield.websocket.WSPacket.PacketType.ERROR
import com.beakshield.websocket.WSPacket.PacketType.PONG
import com.beakshield.websocket.WSPacket.PacketType.SYNC_STATE
import com.beakshield.websocket.WSPacket.PacketType.USER_DATA
import com.beakshield.websocket.WSPacket.PacketType.USER_INPUT_REQUEST_RESPONSE
import com.beakshield.websocket.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Dawson {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val socket = WebSocketClient()

    val _connectionState = socket.connectionState

    val connectionState = socket.connectionState
    private var syncTimerJob: Job? = null
    private var connectTimerJob: Job? = null

    private val _currentUserUUID = MutableStateFlow<String?>(null)
    val currentUserUUID = _currentUserUUID.asStateFlow()

    private val _activeAgents = MutableStateFlow<List<Agent>>(emptyList())
    val activeAgents = _activeAgents.asStateFlow()

    private val _activeUsers = MutableStateFlow<List<User>>(emptyList())
    val users = _activeUsers.asStateFlow()

    private val _activeChats = MutableStateFlow<List<Chat>>(emptyList())
    val activeChats = _activeChats.asStateFlow()

    private val _activeProviders = MutableStateFlow<List<Provider>>(emptyList())
    val activeProviders = _activeProviders.asStateFlow()

    private val _pendingInputRequests = MutableStateFlow<List<UserInputRequest>>(emptyList())
    val pendingInputRequests = _pendingInputRequests.asStateFlow()

    init {
        _activeUsers.update { it + User.defaultUser }          // USED FOR TESTING (NOT PRODUCTION)
        _currentUserUUID.value = User.defaultUser.uuid      // USED FOR TESTING (NOT PRODUCTION)

        scope.launch {
            socket.incomingPackets.collect { packet ->
                println("Packet received from DAWSON server: TYPE: ${packet.type}, PAYLOAD: ${packet.payload.toString().take(100)}")
                when (packet.type) {
                    PONG -> {
                        println("Server pong")
                    }
                    SYNC_STATE -> {
                        packet.payloadAs<SyncState>()?.let {
                            handleSyncState(it)
                        }
                    }
                    USER_DATA -> {
                        packet.payloadAs<UserData>()?.let {
                            handleUserData(it)
                        }
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
            connectionState.collect { state ->
                if (!prevConn && (state.state == ServerConnState.ConnState.CONNECTED)) {
                    fetchAll()
                    startSyncTimer()
                }
                prevConn = (state.state == ServerConnState.ConnState.CONNECTED)
            }
        }

        startConnectTimer()
    }

    fun connect() {
        socket.connect(
            preferences.serverAddress,
            preferences.serverPort,
            preferences.serverAuthToken,
            preferences.serverFingerprint
        )
    }

    fun disconnect() {
        socket.disconnect()
    }

    private fun startConnectTimer() {
        if (connectTimerJob != null) return
        connectTimerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                if (connectionState.value.state != ServerConnState.ConnState.CONNECTED) {
                    connect()
                }
                delay(1500)
            }
        }
    }

    private fun startSyncTimer() {
        if (syncTimerJob != null) return
        syncTimerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                println("Syncing app with DAWSON Server")
                fetchSyncStates()
                fetchProviders()    // Remove when/if server setup to persist providers list
                delay(5000L)
            }
        }
    }

    fun respondToRequest(response: UserInputResponse) {
        _pendingInputRequests.update { requests ->
            requests.filterNot { it.agentUUID == response.agentUUID }
        }

        socket.send(response, UserInputResponse::class, USER_INPUT_REQUEST_RESPONSE)
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
        _activeChats.update { it.toList() }
        val userData = UserData(message, chatUUID)
        socket.send(userData, UserData::class, USER_DATA)
    }

    fun updateProviderAPIKeys(apiKeys: Map<Provider.ProviderType, String>) {
        val userUUID = _currentUserUUID.value ?: return
        val payload = WebSocketClient.json.encodeToJsonElement(serializer<Map<Provider.ProviderType, String>>(), apiKeys)
        val configData = ConfigData(userUUID, dataType = ConfigData.DataType.UPDATE_PROVIDER, payload = payload)
        socket.send(configData, ConfigData::class, CONFIG_DATA)
    }

    fun deleteChat(chat: Chat) {
        val userUUID = _currentUserUUID.value ?: return
        val payload = WebSocketClient.json.encodeToJsonElement(serializer<String>(), chat.uuid)
        val chatData = ChatData(chat.uuid, userUUID, chat.agentUUID, ChatData.DataType.DELETE_CHAT, payload)
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    fun updateChat(chat: Chat) {
        val userUUID = _currentUserUUID.value ?: return
        val updatedChat = _activeChats.value.firstOrNull { it.uuid == chat.uuid }?.copy(
            title = chat.title,
            subtitle = chat.subtitle
        ) ?: return
        val payload = WebSocketClient.json.encodeToJsonElement(serializer<Chat>(), updatedChat)
        val chatData = ChatData(chat.uuid, userUUID, chat.agentUUID, ChatData.DataType.UPSERT_CHAT, payload)
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    fun updateAgent(agent: Agent) {
        val userUUID = _currentUserUUID.value ?: return
        val updatedAgent = _activeAgents.value.firstOrNull { it.uuid == agent.uuid }?.copy(
            mode = agent.mode,
            model = agent.model,
            thoughtWindow = agent.thoughtWindow,
            contextWindow = agent.contextWindow,
            useThinking = agent.useThinking,
            directories = agent.directories
        ) ?: return
        val payload = WebSocketClient.json.encodeToJsonElement(serializer<Agent>(), updatedAgent)
        val configData = ConfigData(userUUID, dataType = ConfigData.DataType.UPDATE_AGENT, payload = payload)
        socket.send(configData, ConfigData::class, CONFIG_DATA)
    }

    private fun handleSyncState(data: SyncState) {
        println("handleSyncState")

        data.agentStates.forEach { (uuid, lastUpdated) ->
            _activeAgents.value.firstOrNull { it.uuid == uuid }?.let {
                if (it.updatedTimestamp < lastUpdated) {
                    fetchAgent(it.uuid)
                }
            } ?: run {
                fetchAgent(uuid)
            }
        }
        data.userStates.forEach { (uuid, lastUpdated) ->
            _activeUsers.value.firstOrNull { it.uuid == uuid }?.let {
                if (it.updatedTimestamp < lastUpdated) {
                    fetchUser(it.uuid)
                }
            } ?: run {
                fetchUser(uuid)
            }
        }
        data.providerStates.forEach { (uuid, lastUpdated) ->
            // TODO: When/if server persists provider list then need to change this
        }
        data.chatStates.forEach { (uuid, lastUpdated) ->
            _activeChats.value.firstOrNull { it.uuid == uuid }?.let {
                if (it.updatedTimestamp < lastUpdated) {
                    fetchChat(it.uuid)
                }
            } ?: run {
                fetchChat(uuid)
            }
        }
        data.chatMessageStates.forEach { (uuid, lastUpdated) ->
            _activeChats.value.firstOrNull { it.uuid == uuid }?.let { chat ->
                val newestLocalTimestamp = chat.messages.value.maxOfOrNull { it.updatedTimestamp } ?: 0L
                if (newestLocalTimestamp < lastUpdated) {
                    fetchChatMessages(chat.uuid)
                }
            } ?: run {
                fetchChat(uuid)
            }
        }
    }

    private fun handleUserData(data: UserData) {
        println("handleUserData: ${data.dataType}")
        when (data.dataType) {
            UserData.DataType.TEXT_PROMPT -> {
                val uuid = Message.MsgType.TEXT_PROMPT.getStreamUUID(data.dataUUID)
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.setDelivered(uuid)
            }
            else -> {}
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleAgentData(data: AgentData) {
        when (data.dataType) {
            AgentData.DataType.AGENT_STATE -> {
                val state = data.payloadAs<Agent.AgentState>() ?: return
                updateAgentState(data.agentUUID, state)
            }
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
                    chunks = mutableMapOf(0 to "\n## TOOL RESULT: ${text.take(100)} ${if (text.take(120).count() < text.count()) "..." else ""}} ##\n"),
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

    private fun handleChatData(data: ChatData) {
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
                    println("Synced (1) chat.")
                    val chat = data.payloadAs<Chat>() ?: return
                    upsertChat(chat)
                    fetchChatMessages(chat.uuid)
                } ?: run {
                    val chats = data.payloadAs<List<Chat>>() ?: return
                    chats.forEach { chat ->
                        upsertChat(chat)
                    }
                    _activeChats.value.forEach { chat ->
                        if (chats.none { it.uuid == chat.uuid }) {
                            deleteChat(chat.uuid)
                        }
                    }
                    chats.forEach { chat ->
                        fetchChatMessages(chat.uuid)
                    }
                    println("Synced (${chats.count()}) chats.")
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
                println("Synced (${messageDatas.count()}) messageDatas.")
            }
        }
    }

    private fun handleConfigData(data: ConfigData) {
        println("handleConfigData: ${data.dataType}")
        when (data.dataType) {
            ConfigData.DataType.UPDATE_AGENT -> {
                val agent = data.payloadAs<Agent>() ?: return
                upsertAgent(agent)
            }
            ConfigData.DataType.DELETE_AGENT -> {
                val agentUUID = data.payloadAs<String>() ?: return
                deleteAgent(agentUUID)
            }
            ConfigData.DataType.SYNC_AGENTS -> {
                val agents = data.payloadAs<List<Agent>>() ?: return
                agents.forEach {
                    upsertAgent(it)
                }
                println("Synced (${agents.count()}) agents.")
            }
            ConfigData.DataType.UPSERT_USER -> {
                val user = data.payloadAs<User>() ?: return
                upsertUser(user)
            }
            ConfigData.DataType.DELETE_USER -> {
                val userUUID = data.payloadAs<String>() ?: return
                deleteUser(userUUID)
            }
            ConfigData.DataType.SYNC_USERS -> {
                val users = data.payloadAs<List<User>>() ?: return
                users.forEach {
                    upsertUser(it)
                }
                println("Synced (${users.count()}) users.")
            }
            ConfigData.DataType.UPDATE_PROVIDER -> {
                val provider = data.payloadAs<Provider>() ?: return
                upsertProvider(provider)
            }
            ConfigData.DataType.SYNC_PROVIDERS -> {
                val providers = data.payloadAs<List<Provider>>() ?: return
                providers.forEach {
                    upsertProvider(it)
                }
                println("Synced (${providers.count()}) providers.")
            }
        }
    }

    private fun updateAgentState(agentUUID: String, state: Agent.AgentState) {
        _activeAgents.update { oldAgents ->
            val index = oldAgents.indexOfFirst { it.uuid == agentUUID }
            if (index == -1) {
                oldAgents
            } else {
                val current = oldAgents[index]

                current.state = state
                current.updatedTimestamp = Clock.System.now().toEpochMilliseconds()

                oldAgents.toMutableList().apply {
                    this[index] = current
                }
            }
        }
    }

    private fun upsertAgent(agent: Agent) {
        _activeAgents.update { oldAgents ->
            val index = oldAgents.indexOfFirst { it.uuid == agent.uuid }
            if (index == -1) {
                println("Agent (${agent.uuid}) added.")
                (oldAgents + agent)
            } else {
                val current = oldAgents[index]
                val currentTimestamp = current.updatedTimestamp
                if (currentTimestamp >= agent.updatedTimestamp) return@update oldAgents

                current.state = agent.state
                current.mode = agent.mode
                current.model = agent.model
                current.directories = agent.directories
                current.thoughtWindow = agent.thoughtWindow
                current.contextWindow = agent.contextWindow
                current.useThinking = agent.useThinking
                current.updatedTimestamp = Clock.System.now().toEpochMilliseconds()

                oldAgents.toMutableList().apply {
                    this[index] = current
                }
            }
        }
    }

    private fun deleteAgent(agentUUID: String) {
        _activeAgents.update { agents ->
            agents.filterNot { it.uuid == agentUUID }
        }
        println("Agent (${agentUUID}) deleted.")
    }

    private fun upsertUser(user: User) {
        _activeUsers.update { oldUsers ->
            val index = oldUsers.indexOfFirst { it.uuid == user.uuid }
            if (index == -1) {
                println("User (${user.uuid}) added.")
                (oldUsers + user)
            } else {
                val current = oldUsers[index]
                val currentTimestamp = current.updatedTimestamp
                if (currentTimestamp >= user.updatedTimestamp) return@update oldUsers

                current.name = user.name
                current.notes = user.notes
                current.updatedTimestamp = Clock.System.now().toEpochMilliseconds()

                oldUsers.toMutableList().apply {
                    this[index] = current
                }
            }
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

    private fun upsertProvider(provider: Provider) {
        _activeProviders.update { oldProviders ->
            val index = oldProviders.indexOfFirst { it.type == provider.type }
            if (index == -1) {
                println("Provider (${provider.type}) added.")
                (oldProviders + provider)
            } else {
                val currentTimestamp = oldProviders[index].updatedTimestamp
                if (currentTimestamp >= provider.updatedTimestamp) return@update oldProviders

                oldProviders.toMutableList().apply {
                    this[index] = oldProviders[index].copy(
                        apiKey = provider.apiKey,
                        models = provider.models,
                        updatedTimestamp = Clock.System.now().toEpochMilliseconds()
                    )
                }
            }
        }
    }

    private fun upsertChat(chat: Chat) {
        _activeChats.update { oldChats ->
            val index = oldChats.indexOfFirst { it.uuid == chat.uuid }
            if (index == -1) {
                println("Chat (${chat.uuid}) added.")
                (oldChats + chat)
            } else {
                val current = oldChats[index]
                val currentTimestamp = current.updatedTimestamp
                if (currentTimestamp >= chat.updatedTimestamp) return@update oldChats

                current.title = chat.title
                current.subtitle = chat.subtitle
                current.updatedTimestamp = Clock.System.now().toEpochMilliseconds()

                oldChats.toMutableList().apply {
                    this[index] = current
                }
            }
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

    private fun fetchSyncStates() {
        val userUUID = _currentUserUUID.value ?: return

        val data = SyncState(userUUID)
        socket.send(data, SyncState::class, SYNC_STATE)
    }

    private fun fetchAgent(agentUUID: String) {
        val configData = ConfigData(agentUUID = agentUUID, dataType = ConfigData.DataType.SYNC_AGENTS, payload = JsonNull)
        socket.send(configData, ConfigData::class, CONFIG_DATA)
    }

    private fun fetchUser(userUUID: String) {
        val payload = WebSocketClient.json.encodeToJsonElement(serializer<String>(), userUUID)
        val configData = ConfigData(dataType = ConfigData.DataType.SYNC_USERS, payload = payload)
        socket.send(configData, ConfigData::class, CONFIG_DATA)
    }

    private fun fetchChat(chatUUID: String) {
        val userUUID = _currentUserUUID.value ?: return
        val chatData = ChatData(
            chatUUID = chatUUID,
            userUUID = userUUID,
            dataType = DataType.SYNC_CHAT,
            payload = JsonNull
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    private fun fetchChatMessages(chatUUID: String) {
        val userUUID = _currentUserUUID.value ?: return
        val chatData = ChatData(
            chatUUID = chatUUID,
            userUUID = userUUID,
            dataType = DataType.SYNC_CHAT_MESSAGES,
            payload = JsonNull
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    private fun fetchProvider(providerType: Provider.ProviderType) {
        val payload = WebSocketClient.json.encodeToJsonElement(serializer<Provider.ProviderType>(), providerType)
        val configData = ConfigData(dataType = ConfigData.DataType.SYNC_PROVIDERS, payload = payload)
        socket.send(configData, ConfigData::class, CONFIG_DATA)
    }

    private fun fetchAgents() {
        val userUUID = _currentUserUUID.value ?: return
        val configData = ConfigData(userUUID = userUUID, dataType = ConfigData.DataType.SYNC_AGENTS, payload = JsonNull)
        socket.send(configData, ConfigData::class, CONFIG_DATA)
    }

    private fun fetchUsers() {
        val configData = ConfigData(dataType = ConfigData.DataType.SYNC_USERS, payload = JsonNull)
        socket.send(configData, ConfigData::class, CONFIG_DATA)
    }

    private fun fetchProviders() {
        val configData = ConfigData(dataType = ConfigData.DataType.SYNC_PROVIDERS, payload = JsonNull)
        socket.send(configData, ConfigData::class, CONFIG_DATA)
    }

    private fun fetchChats() {
        val userUUID = _currentUserUUID.value ?: return
        val chatData = ChatData(
            userUUID = userUUID,
            dataType = DataType.SYNC_CHAT,
            payload = JsonNull
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    private fun fetchAllChatMessages() {
        val userUUID = _currentUserUUID.value ?: return
        val chatData = ChatData(
            userUUID = userUUID,
            dataType = DataType.SYNC_CHAT_MESSAGES,
            payload = JsonNull
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    private fun fetchAll() {
        fetchAgents()
        fetchUsers()
        fetchProviders()
        fetchChats()
//        fetchAllChatMessages()
    }

    companion object {
        const val PRIMARY_CHAT_UUID = "PRIMARY_CHAT"
        const val PRIMARY_AGENT_UUID = "PRIMARY_AGENT"
    }
}