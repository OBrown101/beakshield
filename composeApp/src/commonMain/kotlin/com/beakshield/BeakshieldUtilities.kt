package com.beakshield

import io.ktor.client.HttpClient
import kotlin.math.round

expect val isJvm: Boolean
expect fun pickFilePath(): String?

expect fun dawsonHttpClient(expectedCertFingerprint: String): HttpClient

fun Int.formatWithSuffix(): String = when {
    this >= 1_000_000_000 -> {
        val v = round(this / 1_000_000_000.0 * 10) / 10.0
        "${v}${if (v % 1.0 == 0.0) "B" else "B"}"
    }
    this >= 1_000_000 -> {
         val v = round(this / 1_000_000.0 * 10) / 10.0
         "${v}${if (v % 1.0 == 0.0) "M" else "M"}"
    }
    this >= 1_000 -> {
         val v = round(this / 1_000.0 * 10) / 10.0
         "${v}${if (v % 1.0 == 0.0) "K" else "K"}"
    }
    else -> this.toString()
}

fun String.capitalizeString(): String = this.lowercase().replaceFirstChar { it.uppercaseChar() }

fun formatCount(count: Int): String {
    // 85049 → "85,049"
    return count.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}

fun formatBytes(bytes: Long): String {
    return when {
        (bytes >= 1_073_741_824L) -> "${((bytes / 1_073_741_824.0) * 10).toLong() / 10.0} GB"
        (bytes >= 1_048_576L) -> "${bytes / 1_048_576L} MB"
        else -> "${bytes / 1024L} KB"
    }
}