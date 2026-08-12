package com.beakshield.memory

import androidx.compose.ui.graphics.Color
import com.beakshield.dangerColor
import com.beakshield.lightGreenColor
import com.beakshield.textColor

data class KnowledgeStatistics(
    val totalKnowledge: Int? = null,
    val domains: Int? = null,
    val topics: Int? = null,
    val largestDomain: String? = null,
    val newThisWeek: String = "---",
    val diaryEntries: Int? = null,
    val lastUpdated: String = "---",
    val storageUsed: String = "---",
    val healthy: Boolean? = null
) {

    val healthStatus: String
        get() = when (healthy) {
            true -> "Healthy"
            false -> "Issues detected"
            null -> "---"
        }

    val healthColor: Color
        get() = when (healthy) {
            true -> lightGreenColor
            false -> dangerColor
            null -> textColor
        }


    object MockKnowledgeStatistics {
        val mockStatistics = listOf(
            KnowledgeStatistics(
                totalKnowledge = 85082,
                domains = 66,
                topics = 18,
                largestDomain = "Technical \u00B7 35,843",
                newThisWeek = "23",
                diaryEntries = 459,
                lastUpdated = "3 minutes ago",
                storageUsed = "657 MB",
                healthy = true
            )
        )
    }
}