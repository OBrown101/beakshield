package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryDrawer(
    val id: String = "",
    val content: String = "",
    val wing: String = "",
    val room: String = "",
    val sourcePath: String = "",
    val sourceFile: String = "",
    val addedBy: String = "",
    val filedAt: String = "",
    val similarity: Double? = null      // search results only
) {

    object MockMemoryEntry {
        val mockEntries = listOf(
            MemoryDrawer(
                id = "drawer-001",
                content = "USBManager Lifecycle Pattern\nLearned the connection lifecycle, error handling, and reconnection strategy for USBManager.",
                wing = "AndroidDev",
                room = "patterns",
                addedBy = "Android Development Squirebot",
                filedAt = "2026-08-04T22:25:00Z"
            ),
            MemoryDrawer(
                id = "drawer-002",
                content = "Compose Navigation Best Practices\nBest practices for navigation in Jetpack Compose Multiplatform applications.",
                wing = "Research",
                room = "compose",
                addedBy = "Research Squirebot",
                filedAt = "2026-08-04T22:14:00Z"
            ),
            MemoryDrawer(
                id = "drawer-003",
                content = "Email Classification Rules\nRules for prioritizing and classifying incoming emails by importance and urgency.",
                wing = "Email",
                room = "rules",
                addedBy = "Email Management Squirebot",
                filedAt = "2026-08-04T21:58:00Z"
            ),
            MemoryDrawer(
                id = "drawer-004",
                content = "BLE Connection Workflow\nWorkflow for scanning, pairing, and maintaining BLE connections on Android.",
                wing = "AndroidDev",
                room = "workflows",
                addedBy = "Android Development Squirebot",
                filedAt = "2026-08-04T21:30:00Z"
            )
        )
    }
}