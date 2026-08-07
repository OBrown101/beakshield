package com.beakshield.websocket.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryQuery(
    val wing: String? = null,
    val room: String? = null,
    val drawerID: String? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val since: String? = null,      // ISO, inclusive (filed_at)
    val before: String? = null,     // ISO, exclusive (filed_at)
    val query: String? = null,      // keywords only, ≤250 chars
    val context: String? = null,    // reserved for re-ranking
    val maxDistance: Double? = null,
    val sourceFile: String? = null  // exact match on a result's sourcePath
) {

}
