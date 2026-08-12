package com.beakshield.websocket.memory

import com.beakshield.DatetimeHandler.formatRelativeTime
import com.beakshield.DatetimeHandler.formatTimestampAbbrev
import com.beakshield.DatetimeHandler.timestampToDatetime
import com.beakshield.memory.Memory.DERIVED_TITLE_CHARS
import com.beakshield.memory.Memory.MAX_TITLE_CHARS
import com.beakshield.memory.Memory.deriveAAAKTitle
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MemoryDrawer(
    val id: String = "",
    val content: String = "",
    val wing: String = "",
    val room: String = "",
    val sourcePath: String = "",
    val sourceFile: String = "",
    val addedBy: String = "",
    val filedAt: String = "",
    val similarity: Double? = null      // search results only
) {

    val title: String
        get() {
            val firstLine = content.lineSequence().firstOrNull()?.trim() ?: ""
            val aaakTitle = deriveAAAKTitle(firstLine)
            return when {
                (aaakTitle != null) -> aaakTitle
                (firstLine.isNotEmpty() && (firstLine.length <= MAX_TITLE_CHARS)) -> firstLine
                else -> content.trim().take(DERIVED_TITLE_CHARS)
            }
        }

    val body: String?
        get() {
            val firstLine = content.lineSequence().firstOrNull()?.trim() ?: ""
            val trimmed = content.trim()
            return when {
                // AAAK/diary entries: everything after the first line reads as body;
                // the pipe-form first line is already summarized by the title.
                (deriveAAAKTitle(firstLine) != null) -> trimmed
                    .removePrefix(firstLine)
                    .trim()
                    .ifEmpty { null }

                else -> trimmed
                    .removePrefix(title)
                    .trim()
                    .ifEmpty { null }
            }
        }

    val filedAtDatetime: String?
        get() = parseDrawerTimestamp(filedAt)?.let { timestampToDatetime(it) }

    val filedAtTimestamp: Long?
        get() = parseDrawerTimestamp(filedAt)

    val filedAtFormatted: String
        get() = formatTimestampAbbrev(filedAtTimestamp, true) ?: filedAt.take(10).ifEmpty { "---" }

    val filedAtFormattedRelative: String
        get() = formatRelativeTime(filedAtTimestamp) ?: filedAt.take(10).ifEmpty { "---" }

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

    object MockMemoryEntry {
        val mockEntries = listOf(
            MemoryDrawer(
                id = "drawer-001",
                content = "USBManager Lifecycle Pattern\nLearned the connection lifecycle, error handling, and reconnection strategy for USBManager.",
                wing = "dev",
                room = "stuff",
                addedBy = "Android Development Squirebot",
                filedAt = "2026-08-04T22:25:00Z"
            ),
            MemoryDrawer(
                id = "drawer-002",
                content = "Compose Navigation Best Practices\nBest practices for navigation in Jetpack Compose Multiplatform applications.",
                wing = "Research",
                room = "compose",
                addedBy = "Research Squirebot",
                filedAt = "2026-08-04T22:14:00Z"
            ),
            MemoryDrawer(
                id = "drawer-003",
                content = "Email Classification Rules\nRules for prioritizing and classifying incoming emails by importance and urgency.",
                wing = "Email",
                room = "rules",
                addedBy = "Email Management Squirebot",
                filedAt = "2026-08-04T21:58:00Z"
            ),
            MemoryDrawer(
                id = "drawer-004",
                content = "BLE Connection Workflow\nWorkflow for scanning, pairing, and maintaining BLE connections on Android.",
                wing = "AndroidDev",
                room = "workflows",
                addedBy = "Android Development Squirebot",
                filedAt = "2026-08-04T21:30:00Z"
            )
        )
    }
}