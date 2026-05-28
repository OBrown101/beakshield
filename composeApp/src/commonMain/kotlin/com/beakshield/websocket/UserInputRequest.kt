package com.beakshield.websocket

import kotlinx.serialization.Serializable

@Serializable
enum class UserRequestType {
    PERMISSION,
    CLARIFICATION,
    CONFIRMATION,
    SELECTION,
    INPUT
}

@Serializable
data class UserInputRequest(
    val agentUUID: String,
    val userUUID: String,
    val type: UserRequestType,
    val prompt: String,
    val toolCallName: String? = null,
    val metadata: Map<String, String> = emptyMap()
)