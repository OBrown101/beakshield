package com.beakshield.dawson

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

data class Agent(
    val agentUUID: String,
    val name: String = "SquireBot",
    val type: AgentType = AgentType.SQUIRE_BOT
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

   enum class AgentType {
       PRIMARY,
       SQUIRE_BOT;
   }

    fun addMessage(newMsg: Message) {
        val updated = _messages.value.toMutableList()
        val index = _messages.value.indexOfFirst { (it.dataUUID == newMsg.dataUUID) && (it.type == newMsg.type) }
        if (index < 0) {
            _messages.value = (updated + newMsg)
        } else {
            updated[index] = updated[index].copy(
                text = updated[index].text + newMsg.text,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            _messages.value = updated
        }
    }
}