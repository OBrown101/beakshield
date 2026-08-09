package com.beakshield.screens.knowledgeScreen.topicViews

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.composables.BasicBox
import com.beakshield.dawsonGold
import com.beakshield.formatCount
import com.beakshield.memory.WingStyle
import com.beakshield.tablecells.TopicCellViewModel
import com.beakshield.textColor
import com.beakshield.textSecondaryColor

@Preview(device = TABLET)
@Composable
fun TopicsView(
    modifier: Modifier = Modifier,
    wing: String = "wing_android.development",
    topicCellViewModels: List<TopicCellViewModel> = TopicCellViewModel.MockTopicCVM.mockTopicCVMs,
    onBack: () -> Unit = {}
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
                    text = WingStyle.displayName(wing),
                    fontFamily = FontFamily.Serif,
                    color = dawsonGold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "${topicCellViewModels.size} topic${if (topicCellViewModels.size == 1) "" else "s"} in this domain.",
                    color = textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(20.dp),
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = dawsonGold
                )
                Text(
                    text = "All Domains",
                    color = dawsonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            columns = GridCells.Adaptive(minSize = 170.dp),
            horizontalArrangement = Arrangement.spacedBy(padBetween.dp),
            verticalArrangement = Arrangement.spacedBy(padBetween.dp)
        ) {
            items(topicCellViewModels, key = { it.id }) { cellViewModel ->
                TopicCard(
                    cellViewModel = cellViewModel
                )
            }
        }
    }
}

@Composable
private fun TopicCard(
    modifier: Modifier = Modifier,
    cellViewModel: TopicCellViewModel
) {
    val topicStyle = cellViewModel.topicStyle

    BasicBox(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { cellViewModel.onSelect() }
                .padding(horizontal = 12.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(topicStyle.color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, topicStyle.color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = topicStyle.icon,
                    contentDescription = null,
                    tint = topicStyle.color
                )
            }
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = cellViewModel.displayName,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = formatCount(cellViewModel.entryCount),
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "knowledge entries",
                color = textSecondaryColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}