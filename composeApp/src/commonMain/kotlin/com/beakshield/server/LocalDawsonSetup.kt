package com.beakshield.server

data class LocalDawsonCredentials(
    val authKey: String,
    val fingerprint: String
)

expect fun isLocalDawsonInstalled(): Boolean    // True if LaunchAgent installed on this machine.

expect suspend fun uninstallLocalDawson(): String?  // Stops server, removes from login startup (null on success)

expect val isLocalServerInstallSupported: Boolean   // JVM-only (currently macOS only)

expect fun locateDawsonCoreDir(): String?           // Returns user's ~/DAWSON folder if exists

expect suspend fun installLocalDawson(coreDirPath: String): String? // Installs + starts the server

// Polls for the server's first-boot credential files.
expect suspend fun awaitLocalDawsonCredentials(coreDirPath: String, timeoutSeconds: Int = 60): LocalDawsonCredentials?