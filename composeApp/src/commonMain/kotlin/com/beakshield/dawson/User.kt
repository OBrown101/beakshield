package com.beakshield.dawson

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class User(
    val uuid: String,
    var name: String,
    var notes: List<String> = emptyList(),   // Short notes Dawson makes about user (e.g. hobbies, personality)
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {

    companion object {
        const val DEFAULT_USER_UUID = "USER"

        val defaultUser = User(DEFAULT_USER_UUID, "")
    }
}