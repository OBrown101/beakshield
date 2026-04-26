package com.beakshield.dawson

import com.beakshield.websocket.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import com.beakshield.user.User
import com.beakshield.websocket.WSPacket
import com.beakshield.websocket.WSPacket.PacketType.PONG
import com.beakshield.websocket.WSPacket.PacketType.AGENT_DATA
import com.beakshield.websocket.WSPacket.PacketType.ERROR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Dawson {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val socket = WebSocketClient()
    val connectionState = socket.connectionState

    private val _agents = MutableStateFlow<List<Agent>>(emptyList())
    val agents = _agents.asStateFlow()

    init {
        _agents.value = (_agents.value + primaryAgent)

        connect()

        scope.launch {
            socket.incomingPackets.collect { packet ->
                println("Packet received from DAWSON server: TYPE: ${packet.type}, PAYLOAD: ${packet.payload}")
                when (packet.type) {
                    PONG -> {
                        println("Server pong")
                    }
                    AGENT_DATA -> {
                        packet.payloadAs(AgentData::class)?.let {
                            handleAgentData(it)
                        }
                    }
                    ERROR -> {}
                    else -> {}
                }
            }
        }
    }

    fun connect() {
        socket.connect()
    }

    fun sendMessage(message: Message) {
        _agents.value.find { it.agentUUID == message.destinationUUID }?.addMessage(message)
        val userData = UserData(message)
        socket.send(userData, UserData::class, WSPacket.PacketType.USER_DATA)
    }

    fun disconnect() {
        socket.disconnect()
    }

    fun handleAgentData(data: AgentData) {
        when (data.dataType) {
            AgentData.DataType.TEXT_THINKING -> {
                val text = data.payloadAs(String::class) ?: return
                val newMessage = Message(
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = Message.MsgType.TEXT_THINKING,
                    text = text
                )
                _agents.value.first { it.agentUUID == data.agentUUID }.addMessage(newMessage)
            }
            AgentData.DataType.TEXT_RESPONSE -> {
                val text = data.payloadAs(String::class) ?: return
                val newMessage = Message(
                    dataUUID = data.dataUUID,
                    sourceUUID = data.agentUUID,
                    destinationUUID = data.userUUID,
                    type = Message.MsgType.TEXT_RESPONSE,
                    text = text
                )
                _agents.value.first { it.agentUUID == data.agentUUID }.addMessage(newMessage)
            }
            AgentData.DataType.DATA_RESPONSE -> {}
            AgentData.DataType.TOOL_CALL -> {}
            AgentData.DataType.TOOL_RESULT -> {}
            AgentData.DataType.ERROR -> {}
        }
    }

    companion object {
        const val PRIMARY_UUID = "PRIMARY"
        private val primaryAgent = Agent(
            agentUUID = PRIMARY_UUID,
            name = "Dawson",
            type = Agent.AgentType.PRIMARY
        )
    }
}