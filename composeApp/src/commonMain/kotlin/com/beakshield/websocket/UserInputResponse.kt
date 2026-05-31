package com.beakshield.websocket

import kotlinx.serialization.Serializable

@Serializable
data class UserInputResponse(
    val agentUUID: String,
    val userUUID: String,
    val accepted: Boolean? = null,
    val responseText: String?
)