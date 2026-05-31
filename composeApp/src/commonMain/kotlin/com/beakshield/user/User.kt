package com.beakshield.user

import kotlin.time.Clock

data class User(
    val uuid: String,
    val name: String,
    var notes: List<String> = emptyList(),   // Short notes Dawson makes about user (e.g. hobbies, personality)
    val updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {

    companion object {
        const val DEFAULT_USER_UUID = "USER"

        val defaultUser = User(DEFAULT_USER_UUID, "")
    }
}
