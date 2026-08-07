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

fun formatTimestamp(lastTimestamp: Long?, includeAll: Boolean = false): String? {
    if (lastTimestamp == null || lastTimestamp == 0L) return null

    val zone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(zone)
    val time = Instant.fromEpochMilliseconds(lastTimestamp).toLocalDateTime(zone)
    val daysBetween = time.date.daysUntil(now.date)
    val formattedTime = time.time.toString().take(5)

    return when {
        daysBetween == 0 -> {
            formattedTime
        }
        daysBetween in 1..6 -> {
            val dayOfWeek = time.dayOfWeek.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
                .take(3)

            if (includeAll) "$dayOfWeek $formattedTime" else dayOfWeek
        }
        time.year == now.year -> {
            val date = "${time.month.number}/${time.day}"
            if (includeAll) "$date $formattedTime" else date
        }
        else -> {
            val date = "${time.month.number}/${time.day}/${time.year}"
            if (includeAll) "$date $formattedTime" else date
        }
    }
}

fun formatRelativeTime(millis: Long?): String? {
    val value = millis ?: return null
    val elapsedMins = (Clock.System.now().toEpochMilliseconds() - value) / 60_000L

    return when {
        (elapsedMins < 1L) -> "Just now"
        (elapsedMins < 60L) -> "$elapsedMins minute${if (elapsedMins == 1L) "" else "s"} ago"
        (elapsedMins < (24L * 60L)) -> {
            val hours = elapsedMins / 60L
            "$hours hour${if (hours == 1L) "" else "s"} ago"
        }
        (elapsedMins < (7L * 24L * 60L)) -> {
            val days = elapsedMins / (24L * 60L)
            "$days day${if (days == 1L) "" else "s"} ago"
        }
        else -> null
    }
}

fun formatCount(count: Int): String {
    // 85049 → "85,049"
    return count.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}