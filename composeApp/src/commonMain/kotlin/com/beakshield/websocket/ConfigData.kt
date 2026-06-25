package com.beakshield.websocket

import com.beakshield.dawson.Provider
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class ConfigData(
    val userUUID: String? = null,
    val agentUUID: String? = null,
    val providerType: Provider.ProviderType? = null,
    val dataType: DataType,
    val payload: JsonElement
) {
    enum class DataType {
        UPDATE_AGENT,
        DELETE_AGENT,
        SYNC_AGENTS,
        UPSERT_USER,
        DELETE_USER,
        SYNC_USERS,
        UPDATE_PROVIDER,
        SYNC_PROVIDERS;
    }

    @OptIn(InternalSerializationApi::class)
    inline fun <reified T> payloadAs(): T? {
        return try {
            Json.decodeFromJsonElement<T>(payload)
        } catch (e: Exception) {
            null
        }
    }
}