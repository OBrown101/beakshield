package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryRoomList(
    val wing: String? = null,   // null = all rooms across wings
    val rooms: List<MemoryCount> = emptyList()
)