package com.beakshield.dawson

import com.beakshield.dawson.Provider.ProviderType
import kotlinx.serialization.Serializable

@Serializable
data class LLMModel(
    val id: String,
    val name: String,
    val provider: ProviderType
) {

    object MockLLMModel {
        val mockLLMModels = listOf(
            LLMModel(
                id = "1",
                name = "gpt-oss-20b-32k-16k:latest",
                provider = ProviderType.OLLAMA
            ),
            LLMModel(
                id = "2423",
                name = "gemma-4:21b",
                provider = ProviderType.OLLAMA
            ),
            LLMModel(
                id = "1",
                name = "gpt-oss:20b",
                provider = ProviderType.OLLAMA
            ),
            LLMModel(
                id = "5678",
                name = "llama3.1:8b",
                provider = ProviderType.OLLAMA
            ),
            LLMModel(
                id = "91011",
                name = "mistral:7b-instruct",
                provider = ProviderType.OLLAMA
            ),
            LLMModel(
                id = "121314",
                name = "phi3:14b",
                provider = ProviderType.OLLAMA
            ),
            LLMModel(
                id = "151617",
                name = "qwen2:7b",
                provider = ProviderType.OLLAMA
            ),
            LLMModel(
                id = "181920",
                name = "gemma2:27b",
                provider = ProviderType.OLLAMA
            ),
            LLMModel(
                id = "2423",
                name = "GPT-4o",
                provider = ProviderType.OPENAI
            ),
            LLMModel(
                id = "23424",
                name = "Claude Sonnet 3.5",
                provider = ProviderType.ANTHROPIC
            )
        )
    }
}