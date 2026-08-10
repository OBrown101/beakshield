package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.classes.KnowledgeStatistics
import com.beakshield.composables.BasicBox
import com.beakshield.dawsonGold
import com.beakshield.formatCount
import com.beakshield.textColor
import com.beakshield.textSecondaryColor

@Preview
@Composable
fun StatisticsView(
    modifier: Modifier = Modifier,
    statistics: KnowledgeStatistics = KnowledgeStatistics.MockKnowledgeStatistics.mockStatistics[0]
) {
    val padBetween = 12

    BasicBox(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = padBetween.dp),
                text = "Knowledge Statistics",
                fontFamily = FontFamily.Serif,
                color = dawsonGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal
            )
            StatisticRow(
                icon = Icons.AutoMirrored.Outlined.ShowChart,
                label = "Total Knowledge",
                value = statistics.totalKnowledge?.takeIf { it > 0 }?.let { formatCount(it) } ?: "---",
                valueSuffix = "entries"
            )
            StatisticRow(
                icon = Icons.Outlined.Hub,
                label = "Domains",
                value = statistics.domains?.takeIf { it > 0 }?.toString() ?: "---"
            )
            StatisticRow(
                icon = Icons.Outlined.AccountTree,
                label = "Topics",
                value = statistics.topics?.takeIf { it > 0 }?.toString() ?: "---"
            )
            StatisticRow(
                icon = Icons.Outlined.Leaderboard,
                label = "Largest Domain",
                value = statistics.largestDomain ?: "---"
            )
            StatisticRow(
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                label = "New This Week",
                value = statistics.newThisWeek
            )
            StatisticRow(
                icon = Icons.Outlined.Book,
                label = "Diary Entries",
                value = statistics.diaryEntries?.takeIf { it > 0 }?.let { formatCount(it) } ?: "---"
            )
            StatisticRow(
                icon = Icons.Outlined.Schedule,
                label = "Last Updated",
                value = statistics.lastUpdated
            )
            StatisticRow(
                icon = Icons.Outlined.Storage,
                label = "Storage Used",
                value = statistics.storageUsed
            )
            StatisticRow(
                icon = Icons.Outlined.Shield,
                label = "Palace Health",
                value = statistics.healthStatus,
                valueColor = statistics.healthColor,
                enableSpacerLine = false
            )
        }
    }
}

@Composable
private fun StatisticRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueSuffix: String? = null,
    valueColor: Color = textColor,
    enableSpacerLine: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(backgroundColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .border(1.dp, borderColor.copy(alpha = 0.65f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = icon,
                    contentDescription = null,
                    tint = dawsonGold
                )
            }
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(textSecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Normal)) { append("$label\n") }
                    withStyle(style = SpanStyle(valueColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)) { append(value) }
                    valueSuffix?.let {
                        withStyle(style = SpanStyle(textSecondaryColor, fontSize = 12.sp, fontWeight = FontWeight.Normal)) { append(" $it") }
                    }
                },
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (enableSpacerLine) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(borderColor.copy(alpha = 0.65f))
            )
        }
    }
}