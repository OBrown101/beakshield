package com.beakshield.dawson

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

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
        ERROR;
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