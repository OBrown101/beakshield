package com.beakshield.screens.chatsScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.composables.TableView
import com.beakshield.dawsonGold
import com.beakshield.dawsonRed
import com.beakshield.primaryColor
import com.beakshield.tablecells.ChatCellViewModel
import com.beakshield.textColor
import com.beakshield.textMutedColor


@Preview
@Composable
fun ChatsTableView(
    modifier: Modifier = Modifier,
    chatCellViewModels: List<ChatCellViewModel> = ChatCellViewModel.MockMsgGroupCVM.mockMsgGroupCVMs
) {
    TableView(
        modifier = modifier,
        cellViewModels = chatCellViewModels,
        cellHeight = { 90.dp },
        borderColor = Color.Transparent,
        enableSwipeLeft = true,
        cellOnClick = { cellViewModel ->
            chatCellViewModels.forEach { if (it.id != cellViewModel.id) it.selected = false }
            cellViewModel.selected = true
            cellViewModel.onSelect()
        },
        cellOnSwipeLeft = { cellViewModel ->
            cellViewModel.onDelete()
        },
    ) { modifier, cellViewModel ->
        ChatTableCell(modifier, cellViewModel)
    }
}

@Composable
fun ChatTableCell(
    modifier: Modifier = Modifier,
    cellViewModel: ChatCellViewModel
) {
    val selectedShape = RoundedCornerShape(12.dp)

    val borderModifier = if (cellViewModel.selected) {
        Modifier.border(
            BorderStroke(1.dp, primaryColor),
            RoundedCornerShape(12.dp)
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(selectedShape)
            .background(if (cellViewModel.selected) dawsonRed else Color.Transparent)
            .then(borderModifier)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(cardColor)
                .border(
                    width = 1.dp,
                    color = if (cellViewModel.selected) dawsonGold else borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // TODO: add chat profile handling (probably be some icon that represents the chat content)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp)
        ) {
            Text(
                modifier = Modifier.padding(bottom = 6.dp),
                text = cellViewModel.chat.title.ifBlank { "---" },
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cellViewModel.chat.subtitle.ifBlank { "---" },
                color = textMutedColor,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = cellViewModel.formattedLastTimestamp(cellViewModel.chat.messages.value.lastOrNull()?.createdTimestamp),
            color = textMutedColor,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}