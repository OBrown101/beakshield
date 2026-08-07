package com.beakshield.websocket.memory

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemoryStatus(
    @SerialName("total_drawers") val totalDrawers: Int = 0,
    val wings: Int = 0,
    val rooms: Int = 0,
    @SerialName("protocol") val memoryProtocol: String = "",
    @SerialName("aaak_dialect") val aaakDialect: String = ""
)