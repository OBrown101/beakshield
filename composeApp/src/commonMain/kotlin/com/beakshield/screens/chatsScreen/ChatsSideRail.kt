package com.beakshield.screens.chatsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.DESKTOP
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.composables.BasicOutlinedSearchBar
import com.beakshield.composables.BasicRoundedIconBtn
import com.beakshield.dawsonGold
import com.beakshield.tablecells.ChatCellViewModel
import com.beakshield.textSecondaryColor

@Preview(device = DESKTOP)
@Composable
fun ChatsSideRail(
    modifier: Modifier = Modifier,
    chatCellViewModels: List<ChatCellViewModel> = ChatCellViewModel.MockMsgGroupCVM.mockMsgGroupCVMs,
    onSearchChanged: (String) -> Unit = {},
    onNewChat: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val padBetween = 15
    val btnTextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = dawsonGold,
        textAlign = TextAlign.Left
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = padBetween.dp)
                .clickable {
                    onBack()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                tint = textSecondaryColor
            )
            Spacer(Modifier.width(8.dp))
            Text(
                modifier = Modifier,
                text = "Back to Home",
                color = textSecondaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider(color = borderColor)
        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(bottom = padBetween.dp),
            text = "Your Chats",
            color = dawsonGold,
            fontSize = 15.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold
        )
        BasicOutlinedSearchBar(
            modifier = Modifier
                .height(65.dp)
                .padding(bottom = padBetween.dp),
            placeholderText = "Search chats...",
            onValueChange = onSearchChanged
        )
        ChatsTableView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = padBetween.dp),
            chatCellViewModels = chatCellViewModels
        )
        BasicRoundedIconBtn(
            modifier = Modifier
                .height(50.dp)
                .padding(horizontal = 10.dp),
            text = "New Chat",
            textStyle = btnTextStyle,
            imageVector = Icons.Rounded.Add,
            imageHeight = 25,
            color = dawsonGold,
            borderColor = dawsonGold,
            bg = cardColor,
            onClick = onNewChat
        )
    }
}