package com.beakshield.websocket

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class AgentData(
    val dataUUID: String,    // Used to group text/data chunks
    val dataIndex: Int,    // Used for text/data chunks (keep track of order)
    val agentUUID: String,
    val userUUID: String,
    val dataType: DataType,
    val payload: JsonElement,
) {
    enum class DataType {
        TEXT_THINKING,
        TEXT_RESPONSE,
        DATA_RESPONSE,
        TOOL_CALL,
        TOOL_RESULT,
        USER_INPUT_REQUEST,
        DATA_LAST_INDEX,
        ERROR;
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