package com.beakshield.websocket

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class MessageData(
    val uuid: String,
    val runUUID: String,
    val timestamp: Long,
    val chatUUID: String? = null,
    val sourceType: SourceType,
    val sourceUUID: String,
    val destinationUUID: String,
    val dataType: DataType,
    val payload: JsonElement
) {

    enum class DataType {
        TEXT,
        DATA;
    }

    enum class SourceType {
        PROMPT,
        RESPONSE;
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