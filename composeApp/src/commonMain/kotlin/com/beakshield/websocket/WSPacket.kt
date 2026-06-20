package com.beakshield.websocket

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class WSPacket(
    val type: PacketType,
    val payload: JsonElement,
    val transferUUID: String? = null,
    val index: Int? = null,
    val total: Int? = null
) {

    enum class PacketType {
        PING,
        PONG,
        USER_DATA,
        AGENT_DATA,
        CHAT_DATA,
        CONFIG_DATA,
        USER_INPUT_REQUEST_RESPONSE,
        ERROR;

        companion object {
            fun fromTitle(title: String): PacketType? {
                return entries.firstOrNull { it.name == title }
            }
        }
    }

    val isChunk: Boolean
        get() = (transferUUID != null) && (index != null) && (total != null)

    @OptIn(InternalSerializationApi::class)
    inline fun <reified T> payloadAs(): T? {
        return try {
            Json.decodeFromJsonElement<T>(payload)
        } catch (e: Exception) {
            null
        }
    }
}