package com.beakshield.websocket.memory

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemoryDeleteResult(
    val success: Boolean = false,
    @SerialName("drawer_id") val drawerId: String = "",
    val error: String? = null
)