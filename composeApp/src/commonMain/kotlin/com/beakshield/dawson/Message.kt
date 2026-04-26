package com.beakshield.dawson

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Message @OptIn(ExperimentalUuidApi::class) constructor(
    val dataUUID: String = Uuid.random().toString(),
    val sourceUUID: String,
    val destinationUUID: String,
    val type: MsgType = MsgType.TEXT_PROMPT,
    var text: String = "",
    var timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {

    enum class MsgType {
        TEXT_PROMPT,
        TEXT_THINKING,
        TEXT_RESPONSE,
        DATA_PROMPT
    }
}