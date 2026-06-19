package com.beakshield

import io.ktor.client.HttpClient
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.Instant

expect val isJvm: Boolean
expect suspend fun pickFilePath(): String?

expect fun dawsonHttpClient(expectedCertFingerprint: String): HttpClient

fun Int.formatWithSuffix(): String = when {
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

fun String.capitalizeString(): String = this.replaceFirstChar { it.uppercaseChar() }

fun formatTimestamp(lastTimestamp: Long?): String? {
    if (lastTimestamp == null || lastTimestamp == 0L) return null

    val zone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(zone)
    val time = Instant.fromEpochMilliseconds(lastTimestamp).toLocalDateTime(zone)
    val daysBetween = time.date.daysUntil(now.date)

    return when {
        daysBetween == 0 -> {
            time.time.toString().take(5)
        }

        daysBetween in 1..6 -> {
            time.dayOfWeek.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
                .take(3)
        }

        time.year == now.year -> {
            "${time.month.number}/${time.day}"
        }

        else -> {
            "${time.month.number}/${time.day}/${time.year}"
        }
    }
}