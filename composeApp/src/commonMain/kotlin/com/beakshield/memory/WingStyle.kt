package com.beakshield.memory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.beakshield.dawsonGold
import com.beakshield.infoColor
import com.beakshield.lightGreenColor
import com.beakshield.secondaryGoldColor
import com.beakshield.tertiaryGoldColor
import kotlin.math.abs

object WingStyle {

    data class Style(
        val icon: ImageVector,
        val color: Color
    )

    // Keyword → style. First match wins; order groups from most to least specific.
    private val keywordStyles = listOf(
        listOf("email", "mail", "inbox") to Style(Icons.Outlined.Email, dawsonGold),
        listOf("ble", "bluetooth", "radio", "hardware", "firmware") to Style(Icons.Outlined.Bluetooth, infoColor),
        listOf("research", "analysis", "learn") to Style(Icons.Outlined.Search, tertiaryGoldColor),
        listOf("diary", "journal", "writing", "write", "blog", "doc") to Style(Icons.Outlined.Book, secondaryGoldColor),
        listOf("plan", "planning", "roadmap", "structure") to Style(Icons.Outlined.AccountTree, infoColor),
        listOf("user", "personal", "preference", "profile") to Style(Icons.Outlined.Person, secondaryGoldColor),
        listOf("code", "dev", "android", "ios", "swift", "kotlin", "api", "backend", "frontend") to Style(Icons.Outlined.Code, lightGreenColor),
        listOf("idea", "insight", "fact") to Style(Icons.Outlined.Lightbulb, dawsonGold)
    )

    private val fallbackIcons = listOf(
        Icons.Outlined.Code,
        Icons.Outlined.AccountTree,
        Icons.Outlined.Email,
        Icons.Outlined.Bluetooth,
        Icons.Outlined.Folder,
        Icons.Outlined.Book,
        Icons.Outlined.Lightbulb
    )

    private val fallbackColors = listOf(
        lightGreenColor,
        infoColor,
        dawsonGold,
        tertiaryGoldColor,
        secondaryGoldColor
    )

    fun styleFor(wing: String): Style {
        val lowercased = wing.lowercase()
        keywordStyles.firstOrNull { (keywords, _) ->
            keywords.any { lowercased.contains(it) }
        }?.let { return it.second }

        // Unmatched wings fall back to a stable hash pick, so a wing always
        // renders the same icon/color even without a keyword hit.
        val hash = if (wing.isEmpty()) 0 else abs(wing.hashCode())
        return Style(
            icon = fallbackIcons[hash % fallbackIcons.size],
            color = fallbackColors[hash % fallbackColors.size]
        )
    }

    // "wing_ai.assistant" → "Ai Assistant"
    fun displayName(wing: String): String {
        return wing
            .removePrefix("wing_")
            .replace('_', ' ')
            .replace('.', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            .ifEmpty { wing }
    }
}