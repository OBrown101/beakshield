package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryWingList(
    val wings: List<MemoryCount> = emptyList()  // Pre-sorted server-side: count desc, then name.
)