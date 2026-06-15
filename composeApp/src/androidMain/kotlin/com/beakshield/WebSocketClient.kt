package com.beakshield

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import okhttp3.CertificatePinner

actual fun dawsonHttpClient(expectedCertFingerprint: String): HttpClient {
    val normalized = expectedCertFingerprint
        .replace(":", "")
        .trim()
        .lowercase()

    return HttpClient(OkHttp) {
        engine {
            config {
                certificatePinner(
                    CertificatePinner.Builder()
                        .add("**", "sha256/$normalized")
                        .build()
                )
            }
        }

        install(WebSockets)
    }
}