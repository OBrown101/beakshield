package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.composables.BasicBox
import com.beakshield.darkGreenColor
import com.beakshield.lightGreenColor
import com.beakshield.tablecells.KnowledgeCellViewModel
import com.beakshield.textColor
import com.beakshield.textSecondaryColor
import org.jetbrains.compose.resources.painterResource

@Preview(widthDp = 800, heightDp = 70)
@Composable
fun KnowledgeTableCell(
    modifier: Modifier = Modifier,
    cellViewModel: KnowledgeCellViewModel = KnowledgeCellViewModel.MockKnowledgeCVM.mockKnowledgeCVMs[0],
    wingRoomChip: Boolean = true
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
            Image(
                modifier = Modifier
                    .size(43.dp),
                painter = painterResource(wingStyle.emblem),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
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
                        modifier = Modifier.weight(1f, fill = false),
                        text = drawer.title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (cellViewModel.isNew) {
                        NewBadge(modifier = Modifier.padding(start = 8.dp))
                    }
                    if (wingRoomChip) {
                        WingRoomChip(
                            modifier = Modifier
                                .padding(start = 8.dp),
                            label = cellViewModel.wingRoomLabel,
                            onClick = cellViewModel.onWingRoomClick
                        )
                    }
                }
                drawer.body?.let { body ->
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
                    .width(150.dp)
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
                    text = drawer.filedAtFormatted,
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