package com.beakshield.screens.mainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beakshield.dawson.Message
import com.beakshield.mainColor
import com.beakshield.offBlackColor
import com.beakshield.screens.Destination
import com.beakshield.user.User
import com.beakshield.viewModels.MainScreenViewModel

@Composable
fun MainScreen(
    mainScreenViewModel: MainScreenViewModel,
    navToScreen: (Destination) -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    val groupedMessages by mainScreenViewModel.groupedMessages.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(offBlackColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groupedMessages.entries.toList()) { entry ->
                    ChatBubble(entry.value)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter prompt...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            mainScreenViewModel.sendTextPrompt(userInput)
                            userInput = ""
                        }
                    },
                    enabled = userInput.isNotBlank()
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(messages: List<Message>) {
    val firstMsgType = messages.firstOrNull()
    val isUser = (firstMsgType?.sourceUUID == User.DEFAULT_USER_UUID)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .background((if (isUser) mainColor else Color.LightGray), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            messages.forEachIndexed { idx, message ->
                if (idx > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                ChatSegment(message, isUser)
            }
        }
    }
}

@Composable
private fun ChatSegment(
    message: Message,
    isUser: Boolean
) {
    val style = when (message.type) {
        Message.MsgType.TEXT_PROMPT  -> FontWeight.Normal
        Message.MsgType.TEXT_THINKING -> FontWeight.Light
        Message.MsgType.TEXT_RESPONSE -> FontWeight.Normal
        Message.MsgType.DATA_PROMPT   -> FontWeight.Medium
    }

    Text(
        modifier = Modifier
            .background(if (isUser) mainColor else Color.LightGray)
            .padding(8.dp),
        text = when (message.type) {
            Message.MsgType.TEXT_THINKING -> "… (thinking…)".takeIf { message.text.isBlank() } ?: message.text
            else -> message.text
        },
        color = if (isUser) Color.White else Color.Black,
        fontWeight = style
    )
}