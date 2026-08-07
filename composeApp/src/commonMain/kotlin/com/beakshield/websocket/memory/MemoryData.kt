package com.beakshield.websocket.memory

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class MemoryData(
    val userUUID: String,
    val dataUUID: String,
    val dataType: DataType,
    val payload: JsonElement
) {
    enum class DataType {
        OVERVIEW,
        LIST_WINGS,
        LIST_ROOMS,
        PAGE_ENTRIES,
        ENTRY,
        SEARCH,
        DELETE;
    }

    @OptIn(InternalSerializationApi::class)
    inline fun <reified T> payloadAs(): T? {
        return try {
            Json.decodeFromJsonElement<T>(payload)
        } catch (e: Exception) {
            println("AgentData payloadAs error: $e")
            null
        }
    }
}
