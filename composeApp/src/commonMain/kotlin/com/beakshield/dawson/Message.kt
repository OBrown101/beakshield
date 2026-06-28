package com.beakshield.dawson

import com.beakshield.dawson.Message.MsgType.DATA_PROMPT
import com.beakshield.dawson.Message.MsgType.DATA_RESPONSE
import com.beakshield.dawson.Message.MsgType.TEXT_PROMPT
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
    val delivered: Boolean = false,
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {

    constructor(messageData: MessageData) : this (
        uuid = messageData.uuid,
        dataUUID = messageData.runUUID,
        sourceUUID = messageData.sourceUUID,
        destinationUUID = messageData.destinationUUID,
        type = when (messageData.sourceType) {
            MessageData.SourceType.PROMPT -> if (messageData.dataType == MessageData.DataType.DATA) DATA_PROMPT else TEXT_PROMPT
            MessageData.SourceType.RESPONSE -> if (messageData.dataType == MessageData.DataType.DATA) DATA_RESPONSE else TEXT_RESPONSE
        },
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
        DATA_PROMPT,
        DATA_RESPONSE;

        val label: String
            get() = when (this) {
                TEXT_PROMPT -> "Text Prompt"
                TEXT_THINKING -> "Thinking"
                TEXT_RESPONSE -> "Response"
                TOOL_CALL_NAME -> "Tool Call"
                TOOL_CALL_RESULT -> "Tool Result"
                DATA_PROMPT -> "Data Prompt"
                DATA_RESPONSE -> "Data Response"
            }

        fun getStreamUUID(uuid: String): String {
            return "${uuid}_${this.name}"
        }
    }

    object MockMessage {
        val mockMessages = listOf(
            Message(
                uuid = "msg-001",
                dataUUID = "run-001",
                sourceUUID = "source-001",
                destinationUUID = "dest-001",
                type = MsgType.TEXT_PROMPT,
                chunks = mutableMapOf(0 to "Hello, world!"),
                numChunks = 1,
                isStream = false,
                delivered = true,
                createdTimestamp = 1680000000000L,
                updatedTimestamp = 1680000000000L
            ),
            Message(
                uuid = "msg-002",
                dataUUID = "run-002",
                sourceUUID = "source-002",
                destinationUUID = "dest-002",
                type = MsgType.TEXT_RESPONSE,
                chunks = mutableMapOf(0 to "Great to hear, thank you"),
                numChunks = 1,
                isStream = false,
                delivered = false,
                createdTimestamp = 1680000010000L,
                updatedTimestamp = 1680000015000L
            ),
            Message(
                uuid = "msg-003",
                dataUUID = "run-003",
                sourceUUID = "source-003",
                destinationUUID = "dest-003",
                type = MsgType.TEXT_RESPONSE,
                chunks = mutableMapOf(0 to "Thanks for your input."),
                numChunks = 1,
                isStream = false,
                delivered = false,
                createdTimestamp = 1680000020000L,
                updatedTimestamp = 1680000022000L
            )
        )
    }
}