package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryDrawerPage(
    val drawers: List<MemoryDrawer> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0
) {
    // Drawer content contains "previews", need request specific drawer by id for full text
}