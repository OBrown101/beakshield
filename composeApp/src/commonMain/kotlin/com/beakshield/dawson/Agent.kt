package com.beakshield.dawson

import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.example_avatar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import kotlin.time.Clock

@Serializable
data class Agent(
    val uuid: String,
    val type: AgentType = AgentType.SQUIREBOT,
    val mode: Mode = Mode.EGG,
    var model: String? = null,
    var directories: List<String> = emptyList(),
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    private val scope = CoroutineScope(Dispatchers.Default)

   enum class AgentType {
       DAWSON,
       SQUIREBOT,
       PAGE;

       val image: DrawableResource
           get() = when (this) {
               else -> Res.drawable.example_avatar
           }
   }

    enum class Mode {
        EGG,
        FLEDGLING,
        WARRIOR,
        ULTIMATE;

        val label: String
            get() = name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } + " Mode"
    }
}