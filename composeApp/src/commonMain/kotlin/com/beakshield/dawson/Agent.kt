package com.beakshield.dawson

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class Agent(
    val uuid: String,
    val type: AgentType,
    val mode: Mode,
    var model: String? = null,
    var directories: List<String> = emptyList(),
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    private val scope = CoroutineScope(Dispatchers.Default)

   enum class AgentType {
       DAWSON,
       SQUIREBOT,
       PAGE;
   }

    enum class Mode {
        EGG,
        FLEDGLING,
        WARRIOR,
        ULTIMATE;
    }
}