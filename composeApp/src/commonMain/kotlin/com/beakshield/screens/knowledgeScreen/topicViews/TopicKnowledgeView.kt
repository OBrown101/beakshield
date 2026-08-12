package com.beakshield.screens.knowledgeScreen.topicViews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.classes.DataStyle
import com.beakshield.composables.BasicRoundedBtn
import com.beakshield.composables.TableView
import com.beakshield.dawsonGold
import com.beakshield.formatCount
import com.beakshield.screens.knowledgeScreen.KnowledgeLoadingContent
import com.beakshield.screens.knowledgeScreen.KnowledgeTableCell
import com.beakshield.tablecells.KnowledgeCellViewModel
import com.beakshield.textSecondaryColor

@Preview(device = TABLET)
@Composable
fun TopicKnowledgeView(
    modifier: Modifier = Modifier,
    wing: String = "wing_android.development",
    room: String = "decisions",
    totalEntries: Int = 214,
    knowledgeCellViewModels: List<KnowledgeCellViewModel> = KnowledgeCellViewModel.MockKnowledgeCVM.mockKnowledgeCVMs,
    canLoadMore: Boolean = false,
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
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
                    text = "${DataStyle.displayName(wing)} \u203A ${DataStyle.displayName(room)}",
                    fontFamily = FontFamily.Serif,
                    color = dawsonGold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "Showing ${knowledgeCellViewModels.size} of ${formatCount(totalEntries)} entries in this topic.",
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
                    text = "Topics",
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
            isLoading = isLoadingMore && knowledgeCellViewModels.isEmpty(),
            isEmpty = knowledgeCellViewModels.isEmpty(),
            emptyMessage = "No entries in this topic yet."
        ) { tableModifier ->
            TopicKnowledgeTableView(
                modifier = tableModifier,
                knowledgeCellViewModels = knowledgeCellViewModels
            )
        }
        if (canLoadMore) {
            BasicRoundedBtn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = padBetween.dp)
                    .height(40.dp),
                text = if (isLoadingMore) "Loading..." else "Load More Entries",
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
                onClick = {
                    if (!isLoadingMore) {
                        onLoadMore()
                    }
                }
            )
        }
    }
}

@Composable
private fun TopicKnowledgeTableView(
    modifier: Modifier = Modifier,
    knowledgeCellViewModels: List<KnowledgeCellViewModel>
) {
    TableView(
        modifier = modifier,
        cellViewModels = knowledgeCellViewModels,
        cellHeight = { 70.dp },
        emptyTableText = "",
        emptyTableTextColor = textSecondaryColor,
        enableOnClick = true,
        enableSwipeLeft = false,
        borderColor = Color.Transparent,
        cellSpacing = 12,
        cellOnClick = { it.onSelect() }
    ) { cellModifier, cell ->
        KnowledgeTableCell(
            modifier = cellModifier,
            cellViewModel = cell,
            wingRoomChip = false
        )
    }
}