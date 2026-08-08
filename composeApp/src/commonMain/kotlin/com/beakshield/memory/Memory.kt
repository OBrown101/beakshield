package com.beakshield.memory

import kotlin.math.roundToInt

object Memory {
    const val MAX_TITLE_CHARS = 80
    const val DERIVED_TITLE_CHARS = 60
    const val MAX_SEARCH_QUERY_CHARS = 250 // schema hard limit
    const val MAX_SEARCH_RESULTS = 100 // schema hard limit

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

    fun similarityLabel(value: Double?): String? {
        // Search results carry a relevance score. The tool docs mix language
        // ("similarity scores" vs cosine distance filtering)
        // interpreting defensively: 0..1 reads as similarity, 1..2 as cosine distance
        // (0=identical, 2=opposite) mapped back to similarity.
        // TODO: pin the interpretation against one live response.
        val score = value ?: return null
        val similarity = when {
            (score in 0.0..1.0) -> score
            (score in 1.0..2.0) -> (1.0 - (score / 2.0))
            else -> return null
        }
        return "${(similarity * 100).roundToInt()}% match"
    }
}