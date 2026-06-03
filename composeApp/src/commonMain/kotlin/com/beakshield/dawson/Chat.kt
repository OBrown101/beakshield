package com.beakshield.dawson

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class Chat(
    val uuid: String,
    val userUUID: String,
    val agentUUID: String,
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {

    @Transient private val _messages = MutableStateFlow<List<Message>>(emptyList())
    @Transient val messages = _messages.asStateFlow()

    fun addPendingMessage(newMsg: Message, dataIndex: Int) {
        _messages.update { messages ->
            val updated = messages.toMutableList()
            val index = updated.indexOfFirst { (it.dataUUID == newMsg.dataUUID) && (it.type == newMsg.type) }

            if (index < 0) {
                updated.add(newMsg)
            } else {
                val old = updated[index]
                val chunks = old.chunks.toMutableMap()
                chunks[dataIndex] = newMsg.chunks[0] ?: return@update messages

                updated[index] = old.copy(
                    chunks = chunks,
                    updatedTimestamp = Clock.System.now().toEpochMilliseconds()
                )
            }
            updated.sortedBy { it.createdTimestamp }
        }
    }

    fun markMessageComplete(dataUUID: String, lastDataIndex: Int?) {
        _messages.update { messages ->
            val updated = messages.toMutableList()
            val index = updated.indexOfFirst {
                it.dataUUID == dataUUID && it.type == Message.MsgType.TEXT_RESPONSE
            }

            if (index >= 0) {
                val old = updated[index]
                val numChunks = lastDataIndex ?: old.chunks.count()
                val completedResponse = old.copy(
                    numChunks = numChunks,
                    updatedTimestamp = Clock.System.now().toEpochMilliseconds()
                )
                updated[index] = completedResponse

                // Message complete, cleans up run artifacts
                updated.removeAll {
                    it.uuid in setOf(
                        Message.MsgType.TEXT_THINKING.getStreamUUID(dataUUID),
                        Message.MsgType.TOOL_CALL_NAME.getStreamUUID(dataUUID),
                        Message.MsgType.TOOL_CALL_RESULT.getStreamUUID(dataUUID),
                    )
                }
            }
            updated
        }
    }

    fun syncMessages(messages: List<Message>) {
        _messages.update { current ->
            val updated = current.toMutableList()
            for (msg in messages) {
                val idx = updated.indexOfFirst {
                    (it.uuid == msg.uuid) || (it.uuid == msg.type.getStreamUUID(msg.dataUUID))
                }

                if (idx < 0) {
                    updated.add(msg)
                } else {
                    val currentTimestamp = updated[idx].updatedTimestamp
                    if (currentTimestamp < msg.updatedTimestamp) {
                        updated[idx] = updated[idx].copy(
                            uuid = uuid,
                            chunks = msg.chunks.toMutableMap(),
                            updatedTimestamp = msg.updatedTimestamp
                        )
                    }
                }
            }
            updated.sortedBy { it.createdTimestamp }
        }
    }

}