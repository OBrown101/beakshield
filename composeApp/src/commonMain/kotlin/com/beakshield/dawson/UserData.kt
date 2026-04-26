package com.beakshield.dawson

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@Serializable
data class UserData(
    val userUUID: String,
    val agentUUID: String,
    val dataType: DataType,
    val payload: JsonElement,
) {

    constructor(message: Message) : this (
        userUUID = message.sourceUUID,
        agentUUID = message.destinationUUID,
        dataType = DataType.fromMsgType(message.type) ?: DataType.TEXT_PROMPT,
        payload = JsonPrimitive(message.text)
    )

    enum class DataType {
        TEXT_PROMPT,
        DATA_PROMPT,
        AGENT_CONFIG;

        companion object {
            fun fromMsgType(msgType: Message.MsgType): DataType? {
                return when (msgType) {
                    Message.MsgType.TEXT_PROMPT -> TEXT_PROMPT
                    Message.MsgType.DATA_PROMPT -> DATA_PROMPT
                    else -> null
                }
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
