package com.beakshield.websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
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
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

class WebSocketClient {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val serializer = WSPacket.serializer()

    private val client = HttpClient {
        install(WebSockets)
    }

    private var session: DefaultClientWebSocketSession? = null

    private val _incomingPackets = MutableSharedFlow<WSPacket>()
    val incomingPackets = _incomingPackets.asSharedFlow()

    private val _connectionState = MutableStateFlow(false)
    val connectionState = _connectionState.asStateFlow()

    fun connect(address: String, port: Int) {
        scope.launch {
            try {
                val url = "ws://$address:$port/dawson"
                client.webSocket(url) {
                    session = this
                    _connectionState.value = true
                    println("Connected to $url")

                    try {
                        for (frame in incoming) {
                            frame as? Frame.Text ?: continue

                            val decoded = json.decodeFromString(serializer, frame.readText())
                            _incomingPackets.emit(decoded)
                        }
                    } catch (e: Exception) {
                        _connectionState.value = false
                    } finally {
                        _connectionState.value = false
                    }
                }
            } catch (e: Exception) {
                println("connect failed: $e")
                _connectionState.value = false
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun <T : Any>send(data: T, dataClass: KClass<T>, packetType: WSPacket.PacketType) {
        scope.launch {
            val packet = WSPacket(packetType, json.encodeToJsonElement(dataClass.serializer(), data))
            val encoded = json.encodeToString(serializer, packet)
            session?.send(Frame.Text(encoded))
        }
    }

    fun disconnect() {
        scope.launch {
            session?.close()
            session = null
            _connectionState.value = false
        }
    }

    companion object {
        val json: Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}