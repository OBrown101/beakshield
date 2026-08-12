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

    fun sanitizeMemoryContent(raw: String): String {
        var cleaned = raw

        // 1. Strip internal citation/search artifacts (both escaped + literal forms)
        cleaned = cleaned.replace(Regex("""\\ue200.*?\\ue201"""), "")
        cleaned = cleaned.replace(Regex("""\ue200[^\ue201]*\ue201"""), "")

        // 2. Unescape double-escaped sequences (mined JSON often carries these)
        cleaned = cleaned
            .replace("\\r\\n", "\n")
            .replace("\\n", "\n")
            .replace("\\t", "    ")
            .replace("\\\"", "\"")

        // 3. Decode \uXXXX escapes (sequential halves reassemble surrogate pairs)
        cleaned = Regex("""\\u([0-9a-fA-F]{4})""").replace(cleaned) { match ->
            match.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: match.value
        }

        // 4. Trim chunk-truncation debris: dangling backslash or half an escape
        //    at either end (chunks cut mid-token)
        cleaned = cleaned
            .trim()
            .trimEnd('\\')
            .removeSuffix("\\u")
            .trim()

        // 5. Fence code-shaped content that isn't already fenced — the single
        //    heuristic that makes rough source-mined drawers render cleanly.
        if (!cleaned.contains("```") && looksLikeCode(cleaned)) {
            cleaned = "```\n$cleaned\n```"
        }

        // 6. Collapse big blank gaps left by removals
        cleaned = cleaned.replace(Regex("""\n{3,}"""), "\n\n").trim()

        return cleaned
    }

    private fun looksLikeCode(text: String): Boolean {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (lines.size < 3) return false

        val structural = lines.count { line ->
            line.endsWith("{") || line.endsWith("}") || line.endsWith(";") ||
                    line.startsWith("import ") || line.startsWith("func ") ||
                    line.startsWith("val ") || line.startsWith("var ") ||
                    line.startsWith("let ") || line.startsWith("class ") ||
                    line.startsWith("private ") || line.startsWith("return") ||
                    line.startsWith("//") || line.startsWith("@") ||
                    line.contains(" = ") && line.contains("(")
        }

        return structural >= (lines.size / 3)
    }
}