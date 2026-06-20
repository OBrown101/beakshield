package com.beakshield

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

actual fun dawsonHttpClient(expectedCertFingerprint: String): HttpClient {
    return HttpClient(CIO) {
        engine {
            https {
                trustManager = pinnedTrustManager(expectedCertFingerprint)
            }
        }

        install(WebSockets) {
            maxFrameSize = 64_000
        }
    }
}

@Suppress("CustomX509TrustManager")
private fun pinnedTrustManager(expectedFingerprint: String): X509TrustManager {
    val normalizedExpected = expectedFingerprint
        .replace(":", "")
        .trim()
        .uppercase()

    return object : X509TrustManager {
        override fun checkClientTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?
        ) = Unit

        override fun checkServerTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?
        ) {
            val cert = chain?.firstOrNull()
                ?: throw CertificateException("Missing server certificate")

            val actual = MessageDigest
                .getInstance("SHA-256")
                .digest(cert.encoded)
                .joinToString("") { "%02X".format(it) }

            if (actual != normalizedExpected) {
                throw CertificateException("DAWSON server certificate fingerprint mismatch")
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }
}