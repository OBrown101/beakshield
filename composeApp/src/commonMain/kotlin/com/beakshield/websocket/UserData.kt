package com.beakshield.websocket

import com.beakshield.dawson.Message
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class UserData(
    val dataUUID: String,
    val chatUUID: String,
    val userUUID: String,
    val agentUUID: String,
    val dataType: DataType,
    val payload: JsonElement,
) {

    constructor(message: Message, chatUUID: String) : this (
        dataUUID = message.dataUUID,
        chatUUID = chatUUID,
        userUUID = message.sourceUUID,
        agentUUID = message.destinationUUID,
        dataType = DataType.fromMsgType(message.type) ?: DataType.TEXT_PROMPT,
        payload = JsonPrimitive(message.chunks[0])
    )

    enum class DataType {
        TEXT_PROMPT,
        DATA_PROMPT;

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
    inline fun <reified T> payloadAs(): T? {
        return try {
            Json.decodeFromJsonElement<T>(payload)
        } catch (e: Exception) {
            null
        }
    }
}