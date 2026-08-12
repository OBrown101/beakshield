package com.beakshield.classes

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
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.coding_icon
import beakshield.composeapp.generated.resources.default_icon
import beakshield.composeapp.generated.resources.hardware_icon
import beakshield.composeapp.generated.resources.ideas_icon
import beakshield.composeapp.generated.resources.mail_icon
import beakshield.composeapp.generated.resources.person_icon
import beakshield.composeapp.generated.resources.planning_icon
import beakshield.composeapp.generated.resources.research_icon
import beakshield.composeapp.generated.resources.writing_icon
import com.beakshield.dawsonGold
import com.beakshield.infoColor
import com.beakshield.lightGreenColor
import com.beakshield.secondaryGoldColor
import com.beakshield.tertiaryGoldColor
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.abs

object DataStyle {

    data class Style(
        val icon: ImageVector,
        val color: Color,
        val emblem: DrawableResource
    )

    private val codingStyle = Style(Icons.Outlined.Code, lightGreenColor, Res.drawable.coding_icon)
    private val mailStyle = Style(Icons.Outlined.Email, dawsonGold, Res.drawable.mail_icon)
    private val researchStyle = Style(
        Icons.Outlined.Search,
        tertiaryGoldColor, Res.drawable.research_icon
    )
    private val hardwareStyle = Style(
        Icons.Outlined.Bluetooth,
        infoColor, Res.drawable.hardware_icon
    )
    private val writingStyle = Style(
        Icons.Outlined.Book,
        secondaryGoldColor, Res.drawable.writing_icon
    )
    private val planningStyle = Style(
        Icons.Outlined.AccountTree,
        infoColor, Res.drawable.planning_icon
    )
    private val personStyle = Style(
        Icons.Outlined.Person,
        secondaryGoldColor, Res.drawable.person_icon
    )
    private val ideasStyle = Style(Icons.Outlined.Lightbulb, dawsonGold, Res.drawable.ideas_icon)
    private val defaultStyle = Style(Icons.Outlined.Folder, dawsonGold, Res.drawable.default_icon)

    private val keywordStyles = listOf(
        listOf("email", "mail", "inbox", "outlook", "gmail") to mailStyle,
        listOf("ble", "bluetooth", "radio", "hardware", "firmware", "usb", "esp32", "nrf", "antenna", "serial") to hardwareStyle,
        listOf("research", "analysis", "analyze", "investigate", "study") to researchStyle,
        listOf("diary", "journal", "writing", "write", "blog", "doc", "notes") to writingStyle,
        listOf("plan", "planning", "roadmap", "schedule", "task", "workflow", "organize") to planningStyle,
        listOf("user", "personal", "preference", "profile", "identity") to personStyle,
        listOf("code", "coding", "dev", "android", "ios", "swift", "kotlin", "python", "api", "backend", "frontend", "debug", "build", "compose") to codingStyle,
        listOf("idea", "insight", "fact", "discovery", "brainstorm") to ideasStyle
    )

    // Unmatched subjects: default emblem, but hash-varied vector icon/color
    // so they still differentiate in small contexts — and stay stable per name.
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

    fun styleFor(key: String): Style {
        val lowercased = key.lowercase()
        keywordStyles.firstOrNull { (keywords, _) ->
            keywords.any { lowercased.contains(it) }
        }?.let { return it.second }

        val hash = if (key.isEmpty()) 0 else abs(key.hashCode())
        return defaultStyle.copy(
            icon = fallbackIcons[hash % fallbackIcons.size],
            color = fallbackColors[hash % fallbackColors.size]
        )
    }

    fun styleForText(text: String, fallbackKey: String): Style {
        val lowercased = text.lowercase()
        keywordStyles.firstOrNull { (keywords, _) ->
            keywords.any { lowercased.contains(it) }
        }?.let { return it.second }
        return styleFor(fallbackKey)
    }

    // "wing_ai.assistant" → "Ai Assistant"; harmless for non-wing names.
    fun displayName(name: String): String {
        return name
            .removePrefix("wing_")
            .replace('_', ' ')
            .replace('.', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            .ifEmpty { name }
    }
}