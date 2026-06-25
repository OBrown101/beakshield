package com.beakshield

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import java.io.File
import java.util.Properties

actual fun createPortableSettings(): Settings {
    val userHome = System.getProperty("user.home")
    val dawsonFolder = File(userHome, "DAWSON")
    if (!dawsonFolder.exists()) {
        dawsonFolder.mkdirs()
    }

    val settingsFile = File(dawsonFolder, "config.properties")

    val properties = Properties()
    if (settingsFile.exists()) {
        settingsFile.inputStream().use { properties.load(it) }
    }

    return PropertiesSettings(properties) {
        // Automatically updates the properties file inside DAWSON whenever a setting changes
        settingsFile.outputStream().use { properties.store(it, "BeakShield DAWSON Portable Config") }
    }
}