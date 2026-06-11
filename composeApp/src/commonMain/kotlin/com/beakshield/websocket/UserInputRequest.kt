package com.beakshield.websocket

import kotlinx.serialization.Serializable

@Serializable
data class UserInputRequest(
    val agentUUID: String,
    val userUUID: String,
    val type: ReqType,
    val prompt: String,
    val toolCallName: String? = null,
    val metadata: Map<String, String> = emptyMap()
){


    @Serializable
    enum class ReqType {
        PERMISSION,
        CONFIRMATION,
        INPUT;

        val binaryResp: Boolean
            get() = when (this) {
                PERMISSION -> true
                CONFIRMATION -> true
                else -> false
            }

        val textResp: Boolean
            get() = when (this) {
                INPUT -> true
                else -> false
            }
    }
}