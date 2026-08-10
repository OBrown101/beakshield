package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryStatus(
    val totalDrawers: Int = 0,
    val wings: Int = 0,
    val rooms: Int = 0,
    val diaryEntries: Int? = null,
    val healthy: Boolean? = null
)