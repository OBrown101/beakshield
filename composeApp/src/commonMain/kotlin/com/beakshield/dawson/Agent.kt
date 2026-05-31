package com.beakshield.dawson

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Agent(
    val uuid: String,
    val type: AgentType,
    val mode: Mode,
    var model: String? = null
) {
    private val scope = CoroutineScope(Dispatchers.Default)

   enum class AgentType {
       AGENT_DAWSON,
       AGENT_SQUIREBOT;
   }

    enum class Mode {
        EGG,
        FLEDGLING,
        WARRIOR,
        ULTIMATE;
    }
}