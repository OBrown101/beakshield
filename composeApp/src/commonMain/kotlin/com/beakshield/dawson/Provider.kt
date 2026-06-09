package com.beakshield.dawson

import com.beakshield.dawson.LLMModel.MockLLMModel
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class Provider(
    val type: ProviderType,
    var apiKey: String = "",
    var models: List<LLMModel> = emptyList(),
    val updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {

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
    }


    companion object {
        val defaultProvider = Provider(
            type = ProviderType.OLLAMA,
            models = emptyList(),
        )
    }

    object MockProvider {
        val mockProviders = listOf(
            Provider(
                type = ProviderType.OLLAMA,
                models = MockLLMModel.mockLLMModels.filter { it.provider == ProviderType.OLLAMA }
            ),
            Provider(
                type = ProviderType.OPENAI,
                models = MockLLMModel.mockLLMModels.filter { it.provider == ProviderType.OPENAI }
            ),
            Provider(
                type = ProviderType.ANTHROPIC,
                models = MockLLMModel.mockLLMModels.filter { it.provider == ProviderType.ANTHROPIC }
            )
        )
    }
}
