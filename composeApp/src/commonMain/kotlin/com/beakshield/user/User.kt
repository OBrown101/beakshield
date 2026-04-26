package com.beakshield.user

data class User(
    val uuid: String,
    val name: String,
) {

    companion object {
        const val DEFAULT_USER_UUID = "USER"
    }
}
