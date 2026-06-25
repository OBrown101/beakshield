package com.beakshield

import com.russhwolf.settings.Settings

expect fun createPortableSettings(): Settings

object GlobalPrefKeys {
    const val IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH"
    const val SERVER_ADDRESS = "SERVER_ADDRESS"
    const val SERVER_PORT = "SERVER_PORT"
    const val SERVER_AUTH_TOKEN = "SERVER_AUTH_TOKEN"
    const val SERVER_FINGERPRINT = "SERVER_FINGERPRINT"
}

class Preferences(
    private val settings: Settings = createPortableSettings()
) {
    var isFirstLaunch: Boolean
        get() = settings.getBoolean(GlobalPrefKeys.IS_FIRST_LAUNCH, true)
        set(value) = settings.putBoolean(GlobalPrefKeys.IS_FIRST_LAUNCH, value)

    var serverAddress: String
        get() = settings.getString(GlobalPrefKeys.SERVER_ADDRESS, "")
        set(value) = settings.putString(GlobalPrefKeys.SERVER_ADDRESS, value)

    var serverPort: Int
        get() = settings.getInt(GlobalPrefKeys.SERVER_PORT, 0)
        set(value) = settings.putInt(GlobalPrefKeys.SERVER_PORT, value)

    var serverAuthToken: String
        get() = settings.getString(GlobalPrefKeys.SERVER_AUTH_TOKEN, "")
        set(value) = settings.putString(GlobalPrefKeys.SERVER_AUTH_TOKEN, value)

    var serverFingerprint: String
        get() = settings.getString(GlobalPrefKeys.SERVER_FINGERPRINT, "")
        set(value) = settings.putString(GlobalPrefKeys.SERVER_FINGERPRINT, value)
}