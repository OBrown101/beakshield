package com.beakshield.user

data class User(
    val uuid: String,
    val name: String,
    var notes: List<String> = emptyList()   // Short notes Dawson makes about user (e.g. hobbies, personality)
) {

    companion object {
        const val DEFAULT_USER_UUID = "USER"
    }
}
