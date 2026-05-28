package com.beakshield.dawson

import com.beakshield.dawson.Agent.Mode
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
import com.beakshield.websocket.WSPacket
import com.beakshield.websocket.WSPacket.PacketType.PONG
import com.beakshield.websocket.WSPacket.PacketType.AGENT_DATA
import com.beakshield.websocket.WSPacket.PacketType.CHAT_DATA
import com.beakshield.websocket.WSPacket.PacketType.USER_DATA
import com.beakshield.websocket.WSPacket.PacketType.USER_INPUT_REQUEST_RESPONSE
import com.beakshield.websocket.WSPacket.PacketType.ERROR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Dawson {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val socket = WebSocketClient()
    val connectionState = socket.connectionState

    private val _agents = MutableStateFlow<List<Agent>>(emptyList())
    val agents = _agents.asStateFlow()

    private val _activeChats = MutableStateFlow<List<Chat>>(emptyList())
    val activeChats = _activeChats.asStateFlow()

    private val _pendingInputRequests = MutableStateFlow<List<UserInputRequest>>(emptyList())
    val pendingInputRequests = _pendingInputRequests.asStateFlow()

    init {
        _agents.value = (_agents.value + primaryAgent)

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
                    activeChats.value.forEach {
                        requestChat(it.uuid)
                        requestSyncChatMessages(it.uuid)
                    }
                }
                prevConn = connected
            }
        }
    }

    fun connect(ipAddress: String) {
        socket.connect(ipAddress)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun startNewChat() {
        val newChat = Chat(Uuid.random().toString(), User.DEFAULT_USER_UUID, Agent.PRIMARY_UUID)
        val chatData = ChatData(
            userUUID = User.DEFAULT_USER_UUID,
            agentUUID = Agent.PRIMARY_UUID,
            dataType = DataType.UPSERT_CHAT,
            payload = Json.encodeToJsonElement(serializer<Chat>(), newChat)
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    fun sendMessage(message: Message, chatUUID: String) {
        _activeChats.value.firstOrNull { it.agentUUID == message.destinationUUID }?.addPendingMessage(message)
        val userData = UserData(message, chatUUID)
        socket.send(userData, UserData::class, USER_DATA)
    }

    fun disconnect() {
        socket.disconnect()
    }

    fun handleChatData(data: ChatData) {
        when (data.dataType) {
            DataType.UPSERT_CHAT -> {
                // TODO: Chat was update from another device, update on this device
                // Full chat class data was sent (minus messages)
            }
            DataType.DELETE_CHAT -> {
                // TODO: Chat was deleted from another device, update on this device
                // Only device uuid was sent
            }
            DataType.SYNC_CHAT_MESSAGES -> {
                val messageDatas = data.payloadAs<List<MessageData>>() ?: return
                val messages = messageDatas.map { Message(it) }
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.syncMessages(messages)
            }
            else -> {}
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
                    text = text
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage)
            }
            AgentData.DataType.TEXT_RESPONSE -> {
                val text = data.payloadAs<String>() ?: return
                val newMessage = Message(
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = Message.MsgType.TEXT_RESPONSE,
                    text = text
                )
                _activeChats.value.firstOrNull { it.agentUUID == data.agentUUID }?.addPendingMessage(newMessage)
            }
            AgentData.DataType.DATA_RESPONSE -> {}
            AgentData.DataType.TOOL_CALL -> {}
            AgentData.DataType.TOOL_RESULT -> {}
            AgentData.DataType.USER_INPUT_REQUEST -> {
                val request = data.payloadAs<UserInputRequest>() ?: return
                _pendingInputRequests.update { it + request }
            }
            AgentData.DataType.ERROR -> {}
        }
    }

    fun respondToRequest(request: UserInputRequest, approved: Boolean?, response: String? = null) {
        _pendingInputRequests.value = _pendingInputRequests.value.filterNot { it.agentUUID == request.agentUUID }

        val payload = UserInputResponse(request.agentUUID, request.userUUID, approved, response)
        socket.send(payload, UserInputResponse::class, USER_INPUT_REQUEST_RESPONSE)
    }

    fun requestChat(chatUUID: String) {
        val chatData = ChatData(
            userUUID = User.DEFAULT_USER_UUID,
            agentUUID = Agent.PRIMARY_UUID,
            dataType = DataType.SYNC_CHAT,
            payload = Json.encodeToJsonElement(serializer<String>(), chatUUID)
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    fun requestSyncChatMessages(chatUUID: String) {
        val chatData = ChatData(
            userUUID = User.DEFAULT_USER_UUID,
            agentUUID = Agent.PRIMARY_UUID,
            dataType = DataType.SYNC_CHAT_MESSAGES,
            payload = Json.encodeToJsonElement(serializer<String>(), chatUUID)
        )
        socket.send(chatData, ChatData::class, CHAT_DATA)
    }

    companion object {
        private val primaryAgent = Agent(
            uuid = Agent.PRIMARY_UUID,
            type = Agent.AgentType.AGENT_DAWSON,
            mode = Mode.EGG
        )
    }
}