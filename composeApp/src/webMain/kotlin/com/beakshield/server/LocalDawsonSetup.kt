package com.beakshield.server

actual fun isLocalDawsonInstalled() = false
actual suspend fun uninstallLocalDawson(): String? = "Not supported on this platform."
actual val isLocalServerInstallSupported: Boolean = false

actual fun locateDawsonCoreDir(): String? = "Not supported on this platform."
actual fun isValidDawsonCoreDir(path: String): Boolean = false
actual suspend fun installLocalDawson(coreDirPath: String): String? =
    "Local server install isn't supported on this platform."
actual suspend fun awaitLocalDawsonCredentials(coreDirPath: String, timeoutSeconds: Int): LocalDawsonCredentials? = null