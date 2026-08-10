package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryOverview(
    val status: MemoryStatus? = null,
    val recents: List<MemoryDrawer> = emptyList(),
    val storageBytes: Long? = null,
    val statusError: String? = null,
    val recentsError: String? = null
)
