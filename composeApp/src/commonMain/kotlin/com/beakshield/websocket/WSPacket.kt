package com.beakshield.websocket

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@Serializable
data class WSPacket(
    val type: PacketType,
    val payload: JsonElement
) {

    enum class PacketType {
        PING,
        PONG,
        USER_DATA,
        AGENT_DATA,
        ERROR;

        companion object {
            fun fromTitle(title: String): PacketType? {
                return entries.firstOrNull { it.name == title }
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun <T : Any>payloadAs(clazz: KClass<T>): T? {
        return try {
            Json.decodeFromJsonElement(clazz.serializer(), payload)
        } catch (e: Exception) {
            null
        }
    }
}