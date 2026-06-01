package com.beakshield.dawson

import com.beakshield.dawson.Message.MsgType.DATA_PROMPT
import com.beakshield.dawson.Message.MsgType.TEXT_RESPONSE
import com.beakshield.websocket.MessageData
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
data class Message(
    val uuid: String,
    val dataUUID: String,
    val sourceUUID: String,
    val destinationUUID: String,
    val type: MsgType = MsgType.TEXT_PROMPT,
    var chunks: MutableMap<Int, String> = mutableMapOf(),
    var numChunks: Int? = null,     // If null, chunk data not complete
    val isStream: Boolean = false,
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {

    constructor(messageData: MessageData) : this (
        uuid = messageData.uuid,
        dataUUID = messageData.runUUID,
        sourceUUID = messageData.sourceUUID,
        destinationUUID = messageData.destinationUUID,
        type = if (messageData.dataType == MessageData.DataType.DATA) DATA_PROMPT else TEXT_RESPONSE,
        chunks = mutableMapOf(0 to (messageData.payloadAs<String>() ?: "")),
        createdTimestamp = messageData.timestamp,
        updatedTimestamp = messageData.timestamp
    )

    enum class MsgType {
        TEXT_PROMPT,
        TEXT_THINKING,
        TEXT_RESPONSE,
        TOOL_CALL_NAME,
        TOOL_CALL_RESULT,
        DATA_PROMPT;

        fun getStreamUUID(uuid: String): String {
            return "${uuid}_${this.name}"
        }
    }
}