package com.beakshield.websocket

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@Serializable
data class ChatData(
    val chatUUID: String? = null,
    val userUUID: String,
    val agentUUID: String? = null,
    val dataType: DataType,
    val payload: JsonElement
) {
    enum class DataType {
        UPSERT_CHAT,
        DELETE_CHAT,
        SYNC_CHAT,
        SYNC_CHAT_MESSAGES;
    }

    @OptIn(InternalSerializationApi::class)
    inline fun <reified T> payloadAs(): T? {
        return try {
            Json.decodeFromJsonElement<T>(payload)
        } catch (e: Exception) {
            null
        }
    }
}