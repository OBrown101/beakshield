package com.beakshield

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

actual fun dawsonHttpClient(expectedCertFingerprint: String): HttpClient {
    val tm = pinnedTrustManager(expectedCertFingerprint)
    val ssl = SSLContext.getInstance("TLS").apply { init(null, arrayOf(tm), null) }
    return HttpClient(OkHttp) {
        engine {
            config {
                sslSocketFactory(ssl.socketFactory, tm)
                hostnameVerifier { _, _ -> true }
            }
        }
        install(WebSockets)
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