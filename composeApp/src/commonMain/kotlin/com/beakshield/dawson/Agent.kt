package com.beakshield.dawson

import androidx.compose.ui.graphics.Color
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.dawson_profile
import beakshield.composeapp.generated.resources.page_profile
import beakshield.composeapp.generated.resources.squirebot_profile
import com.beakshield.capitalizeString
import com.beakshield.infoColor
import com.beakshield.lightGreenColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.DrawableResource
import kotlin.time.Clock

@Serializable
data class Agent(
    val uuid: String,
    val userUUID: String,
    val type: AgentType = AgentType.SQUIREBOT,
    val mode: Mode = Mode.EGG,
    var model: LLMModel,
    var state: AgentState = AgentState.READY,
    var thoughtWindow: Int,
    var contextWindow: Int,
    var useThinking: Boolean = true,
    var directories: List<String> = emptyList(),
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    @Transient private val scope = CoroutineScope(Dispatchers.Default)

    enum class AgentState {
        READY,
        AWAITING_INPUT;

        val label: String
            get() = when (this) {
                READY -> "Ready"
                AWAITING_INPUT -> "Awaiting"
            }

        val color: Color
            get() = when (this) {
                READY -> lightGreenColor
                AWAITING_INPUT -> infoColor
            }

        val message: String
            get() = when (this) {
                READY -> "Agent is online"
                AWAITING_INPUT -> "Agent awaiting input"
            }
    }

    enum class AgentType {
        DAWSON,
        SQUIREBOT,
        PAGE;

        val image: DrawableResource
            get() = when (this) {
                DAWSON -> Res.drawable.dawson_profile
                SQUIREBOT -> Res.drawable.squirebot_profile
                PAGE -> Res.drawable.page_profile
            }
    }

    enum class Mode {
        EGG,
        FLEDGLING,
        WARRIOR,
        ULTIMATE;

        val label: String
            get() = name.capitalizeString() + " Mode"
    }

    object MockAgent {
        val mockAgents = listOf(
            Agent(
                uuid = "2390392039",
                userUUID = "a2342f2",
                model = LLMModel.MockLLMModel.mockLLMModels[0],
                thoughtWindow = 240,
                contextWindow = 28000
            )
        )
    }

}