package com.beakshield.dawson

import com.beakshield.websocket.MessageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class Chat(
    val uuid: String,
    val userUUID: String,
    val agentUUID: String
) {

    @Transient private val _messages = MutableStateFlow<List<Message>>(emptyList())
    @Transient val messages = _messages.asStateFlow()

    fun addPendingMessage(newMsg: Message) {
        val updated = _messages.value.toMutableList()
        val index = _messages.value.indexOfFirst { (it.dataUUID == newMsg.dataUUID) && (it.type == newMsg.type) }
        if (index < 0) {
            _messages.value = (updated + newMsg)
        } else {
            updated[index] = updated[index].copy(
                text = updated[index].text + newMsg.text,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            _messages.value = updated
        }
    }

    fun syncMessages(messages: List<Message>) {
        val updated = _messages.value.toMutableList()
        for (msg in messages) {
            val idx = updated.indexOfFirst { (it.timestamp == msg.timestamp) }

            if (idx < 0) {
                updated.add(msg)    // New message
            } else {
                if (updated[idx].timestamp == msg.timestamp) continue

                updated[idx] = updated[idx].copy(
                    text = msg.text,
                    timestamp = msg.timestamp
                )
            }
        }
        _messages.value = updated
    }

}