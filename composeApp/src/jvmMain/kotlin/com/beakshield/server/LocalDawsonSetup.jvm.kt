package com.beakshield.server

import kotlinx.coroutines.delay
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.io.File

private const val PLIST_LABEL = "com.dawson.server"
private const val CORE_FOLDER = "DAWSON1"   // ~/DAWSON (server expects this root)  TODO: CHANGE BACK ONLY FOR TESTING

actual val isLocalServerInstallSupported: Boolean = (hostOs == OS.MacOS)

actual fun locateDawsonCoreDir(): String? {
    // ~/DAWSON with a server binary else null
    val coreDir = File(System.getProperty("user.home"), CORE_FOLDER)
    return if (isValidCoreDir(coreDir)) coreDir.absolutePath else null
}

actual suspend fun installLocalDawson(coreDirPath: String): String? {
    val coreDir = File(coreDirPath)
    if (!isValidCoreDir(coreDir)) {
        return "The DAWSON folder is incomplete (dawson-server and python-macos are both required)."
    }

    return try {
        val executable = File(coreDir, "dawson-server")
        val logsDir = File(coreDir, "logs").apply { mkdirs() }

        // Downloaded archives quarantine executables; launchd refuses them.
        run("xattr", "-dr", "com.apple.quarantine", coreDir.absolutePath, allowFailure = true)
        run("chmod", "755", executable.absolutePath)

        val plist = plistFile()
        plist.parentFile.mkdirs()
        plist.writeText(launchAgentPlist(executable, coreDir, logsDir))

        run("launchctl", "unload", plist.absolutePath, allowFailure = true)
        run("launchctl", "load", plist.absolutePath)
        null
    } catch (e: Exception) {
        e.message ?: "Installation failed."
    }
}

actual fun isLocalDawsonInstalled(): Boolean = plistFile().exists()

actual suspend fun uninstallLocalDawson(): String? {
    return try {
        val plist = plistFile()
        if (plist.exists()) {
            // bootout terminates the KeepAlive'd process now; unload kept as
            // a fallback for older macOS. Both tolerated failing on a
            // half-installed state.
            val uid = run("id", "-u", capture = true).trim()
            run("launchctl", "bootout", "gui/$uid/$PLIST_LABEL", allowFailure = true)
            run("launchctl", "unload", plist.absolutePath, allowFailure = true)
            plist.delete()
        }
        null
    } catch (e: Exception) {
        e.message ?: "Failed to stop the server."
    }
}

actual suspend fun awaitLocalDawsonCredentials(coreDirPath: String, timeoutSeconds: Int): LocalDawsonCredentials? {
    // Poll until both exist and are non-empty.
    val coreDir = File(coreDirPath)
    val tokenFile = File(coreDir, "databank/security/auth-token.txt")
    val fingerprintFile = File(coreDir, "databank/security/auth-fingerprint.txt")

    repeat(timeoutSeconds) {
        if (tokenFile.isFile && fingerprintFile.isFile) {
            val token = tokenFile.readText().trim()
            val fingerprint = fingerprintFile.readText().trim()
            if (token.isNotEmpty() && fingerprint.isNotEmpty()) {
                return LocalDawsonCredentials(authKey = token, fingerprint = fingerprint)
            }
        }
        delay(1000)
    }
    return null
}

// MARK: - Helpers

private fun isValidCoreDir(dir: File): Boolean {
    return (dir.isDirectory && File(dir, "dawson-server").isFile)
}

private fun plistFile(): File =
    File(System.getProperty("user.home"), "Library/LaunchAgents/$PLIST_LABEL.plist")

private fun launchAgentPlist(executable: File, coreDir: File, logsDir: File): String = """
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN"
     "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
    <plist version="1.0">
    <dict>
        <key>Label</key>
        <string>$PLIST_LABEL</string>
        <key>ProgramArguments</key>
        <array>
            <string>${executable.absolutePath}</string>
        </array>
        <key>WorkingDirectory</key>
        <string>${coreDir.absolutePath}</string>
        <key>RunAtLoad</key>
        <true/>
        <key>KeepAlive</key>
        <true/>
        <key>StandardOutPath</key>
        <string>${logsDir.absolutePath}/dawson.log</string>
        <key>StandardErrorPath</key>
        <string>${logsDir.absolutePath}/dawson-error.log</string>
    </dict>
    </plist>
""".trimIndent()

private fun run(vararg command: String, allowFailure: Boolean = false, capture: Boolean = false): String {
    val process = ProcessBuilder(*command)
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    val code = process.waitFor()
    if ((code != 0) && !allowFailure) {
        throw IllegalStateException("${command.first()} failed ($code): ${output.take(300)}")
    }
    return if (capture) output else ""
}