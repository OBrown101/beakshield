package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.beakshield.composables.BasicRoundedBtn
import com.beakshield.composables.TableView
import com.beakshield.dawsonGold
import com.beakshield.tablecells.KnowledgeCellViewModel
import com.beakshield.textSecondaryColor

@Preview(device = TABLET)
@Composable
fun AllKnowledgeView(
    modifier: Modifier = Modifier,
    knowledgeCellViewModels: List<KnowledgeCellViewModel> = KnowledgeCellViewModel.MockKnowledgeCVM.mockKnowledgeCVMs,
    totalKnowledge: Int = knowledgeCellViewModels.size,
    canLoadMore: Boolean = false,
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val padBetween = 12

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = padBetween.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "All Knowledge",
                    fontFamily = FontFamily.Serif,
                    color = dawsonGold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "$totalKnowledge knowledge ${if (totalKnowledge == 1) "entry" else "entries"} across your kingdom.",
                    color = textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClose() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Back to Recents",
                    color = dawsonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                Icon(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp),
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = dawsonGold
                )
            }
        }
        KnowledgeLoadingContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            isLoading = isLoadingMore && knowledgeCellViewModels.isEmpty(),
            isEmpty = knowledgeCellViewModels.isEmpty(),
            emptyMessage = "No knowledge entries found."
        ) { tableModifier ->
            TableView(
                modifier = tableModifier,
                cellViewModels = knowledgeCellViewModels,
                cellHeight = { 70.dp },
                emptyTableText = "",
                emptyTableTextColor = textSecondaryColor,
                enableOnClick = true,
                enableSwipeLeft = false,
                borderColor = androidx.compose.ui.graphics.Color.Transparent,
                cellSpacing = padBetween,
                cellOnClick = { it.onSelect() }
            ) { cellModifier, cell ->
                KnowledgeTableCell(
                    modifier = cellModifier,
                    cellViewModel = cell
                )
            }
            if (canLoadMore) {
                BasicRoundedBtn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = padBetween.dp)
                        .height(40.dp),
                    text = if (isLoadingMore) "Loading..." else "Load More Knowledge",
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
                    onClick = { if (!isLoadingMore) onLoadMore() }
                )
            }
        }
    }
}
