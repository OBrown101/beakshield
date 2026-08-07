package com.beakshield.memory

import kotlin.time.Instant

object Memory {
    const val MAX_TITLE_CHARS = 80
    const val DERIVED_TITLE_CHARS = 60

    fun deriveAAAKTitle(firstLine: String): String? {
        // Diary/AAAK previews look like:
        // "SESSION:2026-06-22|issue.chat.subtitle|User asked why..."
        // Title = first meaningful pipe segment after the SESSION token.
        if (!firstLine.startsWith("SESSION", ignoreCase = true)) return null

        val segment = firstLine
            .split('|')
            .drop(1)
            .map { it.trim().trim('\u2605', '\u2606') }     // strip AAAK emotion stars
            .firstOrNull { it.isNotEmpty() }
            ?: return null

        return segment
            .replace('.', ' ')
            .replace(':', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            .take(MAX_TITLE_CHARS)
    }

    fun parseDrawerTimestamp(timestamp: String): Long? {
        return try {
            Instant.parse(timestamp).toEpochMilliseconds()
        } catch (e: Exception) {
            // Palace timestamps have no zone suffix (e.g. 2026-06-22T13:22:55.234743)
            try {
                Instant.parse(timestamp + "Z").toEpochMilliseconds()
            } catch (e: Exception) {
                null
            }
        }
    }
}