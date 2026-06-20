package com.beakshield.websocket

import androidx.compose.ui.graphics.Color
import com.beakshield.dangerColor
import com.beakshield.dawsonHttpClient
import com.beakshield.lightGreenColor
import com.beakshield.websocket.ServerConnState.ConnState.CONNECTED
import com.beakshield.websocket.ServerConnState.ConnState.DISCONNECTED
import com.beakshield.websocket.ServerConnState.ConnState.ERROR
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.bearerAuth
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ServerConnState(
    var state: ConnState = DISCONNECTED,
    private val msg: String = ""
) {
    enum class ConnState {
        CONNECTED,
        DISCONNECTED,
        ERROR;
    }

    val color: Color
        get() = when (state) {
            CONNECTED -> lightGreenColor
            DISCONNECTED, ERROR -> dangerColor
        }

    val message: String
        get() = when (state) {
            CONNECTED -> "Connected"
            DISCONNECTED -> "Disconnected"
            ERROR -> "Error"
        }

    val description: String
        get() = when (state) {
            CONNECTED -> "Your server is currently configured and connected."
            DISCONNECTED -> "Enter your server information and connect."
            ERROR -> msg
        }
}

class WebSocketClient {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val serializer = WSPacket.serializer()

    private var client: HttpClient? = null
    private var session: DefaultClientWebSocketSession? = null

    private val maxPacketChars = 32_000
    private val chunkBuffers = mutableMapOf<String, MutableList<String?>>()

    private val _incomingPackets = MutableSharedFlow<WSPacket>()
    val incomingPackets = _incomingPackets.asSharedFlow()

    private val _connectionState = MutableStateFlow(ServerConnState())
    val connectionState = _connectionState.asStateFlow()

    fun connect(address: String, port: Int, auth: String, fingerprint: String) {
        scope.launch {
            try {
                val url = "wss://$address:$port/dawson"

                client?.close()
                client = dawsonHttpClient(fingerprint)

                client?.webSocket(url,
                    request = {
                        bearerAuth(auth)
                    }
                ) {
                    session = this
                    setConnState(CONNECTED)
                    println("Connected to $url")

                    try {
                        for (frame in incoming) {
                            frame as? Frame.Text ?: continue

                            handleIncomingText(frame.readText())
                        }
                    } catch (e: Exception) {
                        setConnState(ERROR, e.message ?: "")
                    } finally {
                        setConnState(DISCONNECTED)
                    }
                }
            } catch (e: Exception) {
                println("connect failed: $e")
                setConnState(ERROR, e.message ?: "")
            }
        }
    }

    private suspend fun handleIncomingText(text: String) {
        val packet = json.decodeFromString(serializer, text)

        if (packet.isChunk) {
            val transferUUID = packet.transferUUID ?: return
            val index = packet.index ?: return
            val total = packet.total ?: return
            val chunkText = packet.payloadAs<String>() ?: return

            val buffer = chunkBuffers.getOrPut(transferUUID) {
                MutableList(total) { null }
            }

            if (index !in buffer.indices) return
            buffer[index] = chunkText

            if (buffer.all { it != null }) {
                chunkBuffers.remove(transferUUID)

                val reassembled = buffer.joinToString("") { it ?: "" }
                handleIncomingText(reassembled)
            }
            return
        }

        _incomingPackets.emit(packet)
    }

    @OptIn(InternalSerializationApi::class, ExperimentalUuidApi::class)
    fun <T : Any> send(data: T, dataClass: KClass<T>, packetType: WSPacket.PacketType) {
        scope.launch {
            val packet = WSPacket(
                type = packetType,
                payload = json.encodeToJsonElement(dataClass.serializer(), data)
            )
            sendPacket(packet)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun sendPacket(packet: WSPacket) {
        val encoded = json.encodeToString(serializer, packet)

        if (encoded.length <= maxPacketChars) {
            session?.send(Frame.Text(encoded))
            return
        }

        val transferUUID = Uuid.random().toString()
        val chunks = encoded.chunked(maxPacketChars)

        chunks.forEachIndexed { index, chunkText ->
            val chunkPacket = WSPacket(
                type = packet.type,
                payload = JsonPrimitive(chunkText),
                transferUUID = transferUUID,
                index = index,
                total = chunks.size
            )

            val encodedChunk = json.encodeToString(serializer, chunkPacket)
            session?.send(Frame.Text(encodedChunk))
        }
    }

    fun disconnect() {
        scope.launch {
            session?.close()
            session = null
            client?.close()
            client = null
            setConnState(DISCONNECTED)
        }
    }

    private fun setConnState(state: ServerConnState.ConnState, msg: String = "") {
        _connectionState.value = ServerConnState(state, msg)
    }

    companion object {
        val json: Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}