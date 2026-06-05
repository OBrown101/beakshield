package com.beakshield.screens.mainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.borderColor
import com.beakshield.composables.BasicBox
import com.beakshield.dawsonGold
import com.beakshield.surfaceColor

@Composable
fun DashboardStatus(
    modifier: Modifier,
    onViewAgents: () -> Unit,
    onViewDawsonTasks: () -> Unit,
    onViewActivity: () -> Unit
) {
    val scrollState = rememberScrollState()
    val padBetween = 12

    Column(
        modifier = modifier.verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = padBetween.dp),
            horizontalArrangement = Arrangement.spacedBy(padBetween.dp)
        ) {
            StatusBox(
                modifier = Modifier
                    .weight(1f),
                title = "Active Agents",
                viewAllText = "View all agents",
                quantity = 0,
                tableView = {},
                onViewAll = onViewAgents
            )
            StatusBox(
                modifier = Modifier
                    .weight(1f),
                title = "DAWSON Tasks",
                viewAllText = "View all tasks",
                quantity = 0,
                tableView = {},
                onViewAll = onViewDawsonTasks
            )
        }
        StatusBox(
            modifier = Modifier,
            title = "Recent Activity",
            viewAllText = "View all activity",
            quantity = 0,
            tableView = {},
            onViewAll = onViewActivity
        )
    }
}

@Composable
fun StatusBox(
    modifier: Modifier,
    title: String,
    viewAllText: String,
    quantity: Int,
    tableView: @Composable () -> Unit,
    onViewAll: () -> Unit
) {
    BasicBox(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Serif,
                    color = dawsonGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(surfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = quantity.toString(),
                        fontFamily = FontFamily.Serif,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            HorizontalDivider(color = borderColor)

            tableView()
            Spacer(Modifier.weight(1f))   // Remove this once has table

            HorizontalDivider(color = borderColor)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .clickable {
                        onViewAll()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewAllText,
                    fontFamily = FontFamily.Serif,
                    color = dawsonGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "→",
                    fontFamily = FontFamily.Serif,
                    color = dawsonGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}