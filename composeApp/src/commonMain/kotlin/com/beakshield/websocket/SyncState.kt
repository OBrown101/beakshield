package com.beakshield.websocket

import kotlinx.serialization.Serializable

@Serializable
data class SyncState(
    val userUUID: String,
    val agentStates: Map<String, Long> = emptyMap(),    // (UUID to updatedTimestamp)
    val userStates: Map<String, Long> = emptyMap(),
    val providerStates: Map<String, Long> = emptyMap(),
    val chatStates: Map<String, Long> = emptyMap(),
    val chatMessageStates: Map<String, Long> = emptyMap()
) {

}
