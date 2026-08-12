package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryDeleteResult(
    val success: Boolean = false,
    val drawerId: String = "",
    val error: String? = null
)