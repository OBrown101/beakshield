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
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.composables.BasicBox
import com.beakshield.composables.BasicRoundedBtn
import com.beakshield.dawsonGold
import com.beakshield.textColor
import com.beakshield.textSecondaryColor

@Preview
@Composable
fun StatisticsView(
    modifier: Modifier = Modifier,
    totalKnowledge: Int = 2431,
    domains: Int = 18,
    lastUpdated: String = "3 minutes ago",
    storageUsed: String = "248 MB",
    onViewFullStats: () -> Unit = {}
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
                value = totalKnowledge.takeIf { it > 0 }?.toString() ?: "---",
                valueSuffix = "entries"
            )
            StatisticRow(
                icon = Icons.Outlined.Hub,
                label = "Domains",
                value = domains.toString()
            )
            StatisticRow(
                icon = Icons.Outlined.Schedule,
                label = "Last Updated",
                value = lastUpdated
            )
            StatisticRow(
                icon = Icons.Outlined.Storage,
                label = "Storage Used",
                value = storageUsed
            )

            Spacer(Modifier.height(padBetween.dp))

            BasicRoundedBtn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                text = "View Full Stats",
                borderRadius = 8,
                textStyle = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dawsonGold,
                    textAlign = TextAlign.Center
                ),
                color = dawsonGold,
                borderColor = borderColor,
                bg = cardColor,
                onClick = onViewFullStats
            )
        }
    }
}

@Composable
private fun StatisticRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueSuffix: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(backgroundColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .border(1.dp, borderColor.copy(alpha = 0.65f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    imageVector = icon,
                    contentDescription = null,
                    tint = dawsonGold
                )
            }
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(textSecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Normal)) { append("$label\n") }
                    withStyle(style = SpanStyle(textColor, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)) { append(value) }
                    valueSuffix?.let {
                        withStyle(style = SpanStyle(textSecondaryColor, fontSize = 12.sp, fontWeight = FontWeight.Normal)) { append(" $it") }
                    }
                },
                lineHeight = 21.sp
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(borderColor.copy(alpha = 0.65f))
        )
    }
}