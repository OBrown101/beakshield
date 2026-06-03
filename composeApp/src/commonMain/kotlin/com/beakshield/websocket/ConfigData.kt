package com.beakshield.websocket

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class ConfigData(
    val userUUID: String,
    val dataType: DataType,
    val payload: JsonElement
) {
    enum class DataType {
        UPSERT_AGENT,
        DELETE_AGENT,
        UPSERT_USER,
        DELETE_USER;
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