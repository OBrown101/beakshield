package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.composables.BasicBox
import com.beakshield.composables.TableView
import com.beakshield.darkGreenColor
import com.beakshield.dawsonGold
import com.beakshield.lightGreenColor
import com.beakshield.tablecells.KnowledgeCellViewModel
import com.beakshield.textColor
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
    TableView(
        modifier = modifier,
        cellViewModels = knowledgeCellViewModels,
        cellHeight = { 95.dp },
        emptyTableText = "Nothing learned yet",
        emptyTableTextColor = textSecondaryColor,
        enableOnClick = true,
        enableSwipeLeft = false,
        borderColor = Color.Transparent,
        cellSpacing = 12,
        cellOnClick = { it.onSelect() }
    ) { cellModifier, cell ->
        RecentKnowledgeTableCell(
            modifier = cellModifier,
            cellViewModel = cell
        )
    }
}

@Composable
private fun RecentKnowledgeTableCell(
    modifier: Modifier = Modifier,
    cellViewModel: KnowledgeCellViewModel
) {
    val drawer = cellViewModel.drawer
    val wingStyle = cellViewModel.wingStyle

    BasicBox(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(wingStyle.color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, wingStyle.color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = wingStyle.icon,
                    contentDescription = null,
                    tint = wingStyle.color
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 15.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cellViewModel.title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (cellViewModel.isNew) {
                        NewBadge(modifier = Modifier.padding(start = 8.dp))
                    }
                    WingRoomChip(
                        modifier = Modifier.padding(start = 8.dp),
                        label = cellViewModel.wingRoomLabel,
                        onClick = cellViewModel.onWingRoomClick
                    )
                }
                cellViewModel.body?.let { body ->
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = body,
                        color = textSecondaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(borderColor.copy(alpha = 0.65f))
            )
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .padding(start = 15.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = wingStyle.color
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = drawer.addedBy.ifEmpty { "---" },
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    modifier = Modifier.padding(start = 26.dp, top = 3.dp),
                    text = cellViewModel.filedAtFormatted,
                    color = textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun NewBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(darkGreenColor.copy(alpha = 0.6f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = "New",
            color = lightGreenColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WingRoomChip(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor.copy(alpha = 0.55f))
            .border(1.dp, borderColor.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textSecondaryColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
    }
}