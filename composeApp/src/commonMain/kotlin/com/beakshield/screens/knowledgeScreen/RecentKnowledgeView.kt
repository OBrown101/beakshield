package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.dawsonGold
import com.beakshield.tablecells.KnowledgeCellViewModel
import com.beakshield.textSecondaryColor


@Preview(device = TABLET)
@Composable
fun RecentKnowledgeView(
    modifier: Modifier = Modifier,
    knowledgeCellViewModels: List<KnowledgeCellViewModel> = KnowledgeCellViewModel.MockKnowledgeCVM.mockKnowledgeCVMs,
    onViewAll: () -> Unit = {}
) {
    val padBetween = 12

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = padBetween.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Recent Knowledge",
                    fontFamily = FontFamily.Serif,
                    color = dawsonGold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "Recently learned or referenced knowledge.",
                    color = textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onViewAll() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All",
                    color = dawsonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = dawsonGold
                )
            }
        }
        RecentKnowledgeTableView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            knowledgeCellViewModels = knowledgeCellViewModels
        )
    }
}

@Composable
private fun RecentKnowledgeTableView(
    modifier: Modifier = Modifier,
    knowledgeCellViewModels: List<KnowledgeCellViewModel>
) {
    val cellHeight = 70
    val padding = 12
    BoxWithConstraints(modifier = modifier) {
        val visibleCells = (maxHeight / (cellHeight + padding).dp).toInt().coerceAtLeast(1)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            knowledgeCellViewModels.take(visibleCells).forEach { cell ->
                KnowledgeTableCell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(height = cellHeight.dp, width = Dp.Unspecified)
                        .clickable { cell.onSelect() },
                    cellViewModel = cell
                )
            }
        }
    }
}