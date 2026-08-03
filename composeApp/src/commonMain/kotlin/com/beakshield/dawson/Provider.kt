package com.beakshield.dawson

import com.beakshield.dawson.LLMModel.MockLLMModel
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class Provider(
    val type: ProviderType,
    var apiKey: String = "",
    var useOAuth: Boolean = false,
    var availableModels: List<LLMModel> = emptyList(),
    var preferredModelIDs: List<String> = emptyList(),
    var defaultModelID: String = "",
    val updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {

    val preferredModels: List<LLMModel>
        get() = availableModels.filter { preferredModelIDs.contains(it.id) }

    val defaultModel: LLMModel?
        get() = availableModels.firstOrNull { it.id == defaultModelID }

    @Serializable
    enum class ProviderType {
        OLLAMA,
        OPENAI,
        ANTHROPIC;

        val label: String
            get() = when (this) {
                OLLAMA -> "Ollama"
                OPENAI -> "OpenAI"
                ANTHROPIC -> "Anthropic"
            }

        val initials: String
            get() = when (this) {
                OLLAMA -> "O"
                OPENAI -> "OA"
                ANTHROPIC -> "A"
            }

        val supportsOAuth: Boolean
            get() = when (this) {
                OLLAMA -> false
                OPENAI -> true
                ANTHROPIC -> false
            }

        val supportsContextWindow: Boolean
            get() = when (this) {
                OLLAMA -> true
                OPENAI -> false
                ANTHROPIC -> false
            }

        companion object {
            fun fromString(label: String): ProviderType? {
                return when (label) {
                    OLLAMA.label -> OLLAMA
                    OPENAI.label -> OPENAI
                    ANTHROPIC.label -> ANTHROPIC
                    else -> null
                }
            }

        }
    }

    object MockProvider {
        val mockProviders = listOf(
            Provider(
                type = ProviderType.OLLAMA,
                availableModels = MockLLMModel.mockLLMModels.filter { it.provider == ProviderType.OLLAMA }
            ),
            Provider(
                type = ProviderType.OPENAI,
                availableModels = MockLLMModel.mockLLMModels.filter { it.provider == ProviderType.OPENAI }
            ),
            Provider(
                type = ProviderType.ANTHROPIC,
                availableModels = MockLLMModel.mockLLMModels.filter { it.provider == ProviderType.ANTHROPIC }
            )
        )
    }
}
