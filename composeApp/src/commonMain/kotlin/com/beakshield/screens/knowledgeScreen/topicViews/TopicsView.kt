package com.beakshield.screens.knowledgeScreen.topicViews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.dawsonGold
import com.beakshield.formatCount
import com.beakshield.classes.DataStyle
import com.beakshield.screens.knowledgeScreen.KnowledgeLoadingContent
import com.beakshield.screens.knowledgeScreen.MemorySummaryCard
import com.beakshield.tablecells.TopicCellViewModel
import com.beakshield.textSecondaryColor

@Preview(device = TABLET)
@Composable
fun TopicsView(
    modifier: Modifier = Modifier,
    wing: String = "wing_android.development",
    topicCellViewModels: List<TopicCellViewModel> = TopicCellViewModel.MockTopicCVM.mockTopicCVMs,
    isLoading: Boolean = false,
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
                    text = DataStyle.displayName(wing),
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
        KnowledgeLoadingContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            isLoading = isLoading,
            isEmpty = topicCellViewModels.isEmpty(),
            emptyMessage = "No topics found."
        ) { contentModifier ->
            LazyVerticalGrid(
                modifier = contentModifier,
            columns = GridCells.Adaptive(minSize = 170.dp),
            horizontalArrangement = Arrangement.spacedBy(padBetween.dp),
            verticalArrangement = Arrangement.spacedBy(padBetween.dp)
        ) {
            items(topicCellViewModels, key = { it.id }) { cellViewModel ->
                MemorySummaryCard(
                    style = cellViewModel.topicStyle,
                    title = cellViewModel.displayName,
                    value = formatCount(cellViewModel.entryCount),
                    subtitle = "knowledge entries",
                    onClick = { cellViewModel.onSelect() }
                )
            }
            }
        }
    }
}
