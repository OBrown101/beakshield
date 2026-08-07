package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemorySearchResults(
    val query: String? = null,
    val results: List<MemoryDrawer> = emptyList()
) {
    // Results contain NO drawerId, must use wing/room to navigate
}