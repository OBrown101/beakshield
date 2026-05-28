package com.beakshield.dawson

import com.beakshield.dawson.Message.MsgType.DATA_PROMPT
import com.beakshield.dawson.Message.MsgType.TEXT_RESPONSE
import com.beakshield.websocket.MessageData
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class Message(
    val dataUUID: String = Uuid.random().toString(),
    val sourceUUID: String,
    val destinationUUID: String,
    val type: MsgType = MsgType.TEXT_PROMPT,
    var text: String = "",
    var timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {

    constructor(messageData: MessageData) : this (
        sourceUUID = messageData.sourceUUID,
        destinationUUID = messageData.destinationUUID,
        type = if (messageData.dataType == MessageData.DataType.DATA) DATA_PROMPT else TEXT_RESPONSE,
        text = messageData.payloadAs<String>() ?: "",
        timestamp = messageData.timestamp
    )

    enum class MsgType {
        TEXT_PROMPT,
        TEXT_THINKING,
        TEXT_RESPONSE,
        DATA_PROMPT;

    }
}