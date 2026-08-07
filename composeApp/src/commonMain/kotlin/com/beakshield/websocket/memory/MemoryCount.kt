package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryCount(
    val name: String = "",
    val count: Int = 0
)