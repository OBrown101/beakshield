package com.beakshield.dawson

import kotlin.time.Clock

data class Server(
    var address: String = "localhost",
    var port: Int = 8080,
    var authKey: String = "",
    var fingerprint: String = "",
    var version: String = "1.0",
    var latencyMs: Int = 0,
    var lastSyncTime: Long = 0L
) {

    object MockServer {
        var mockServers = listOf(
            Server(
                address = "dawson.local",
                port = 443,
                authKey = "a28ls92l",
                fingerprint = "29:23:94:18:29:22",
                version = "2.4.1",
                latencyMs = 18,
                lastSyncTime = Clock.System.now().toEpochMilliseconds()
            )
        )
    }
}