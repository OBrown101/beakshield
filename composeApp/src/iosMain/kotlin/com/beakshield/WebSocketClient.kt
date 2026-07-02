package com.beakshield

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.websocket.WebSockets

actual fun dawsonHttpClient(expectedCertFingerprint: String): HttpClient {
    return HttpClient(Darwin) {
        install(WebSockets)
    }
}