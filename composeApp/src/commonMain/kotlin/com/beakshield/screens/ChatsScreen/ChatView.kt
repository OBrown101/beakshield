package com.beakshield.screens.ChatsScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.example_avatar
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.dangerColor
import com.beakshield.dawson.Message
import com.beakshield.dawson.Message.MsgType.TEXT_PROMPT
import com.beakshield.dawson.Message.MsgType.TEXT_RESPONSE
import com.beakshield.dawson.Message.MsgType.TEXT_THINKING
import com.beakshield.dawson.Message.MsgType.TOOL_CALL_NAME
import com.beakshield.dawson.Message.MsgType.TOOL_CALL_RESULT
import com.beakshield.dawsonGold
import com.beakshield.dawsonRed
import com.beakshield.primaryColor
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor
import org.jetbrains.compose.resources.painterResource


@Composable
fun ChatView(
    modifier: Modifier = Modifier,
    groupedMessages: Map<String, List<Message>>,
    userUUIDSelected: String,
    agentName: String = "Android Agent",
    onSendMessage: (String) -> Unit,
    onAttachClick: () -> Unit = {},
    onMicClick: () -> Unit = {}
) {
    var userInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(26.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(groupedMessages.entries.toList()) { entry ->
                ChatBubbleRow(
                    isUser = (entry.value.firstOrNull()?.sourceUUID == userUUIDSelected),
                    messages = entry.value
                )
            }
        }

        UserInputBar(
            value = userInput,
            onValueChange = { userInput = it },
            placeholder = "Ask agent anything...",
            onAttachClick = onAttachClick,
            onMicClick = onMicClick,
            onSendClick = {
                val trimmed = userInput.trim()
                if (trimmed.isNotEmpty()) {
                    onSendMessage(trimmed)
                    userInput = ""
                }
            }
        )
    }
}

@Composable
private fun ChatBubbleRow(
    isUser: Boolean,
    messages: List<Message>
) {
    val avatarSize = 58

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            ChatAvatar(
                painter = painterResource(Res.drawable.example_avatar),
                size = avatarSize
            )
            Spacer(Modifier.width(18.dp))
        }

        ChatBubble(
            modifier = Modifier.widthIn(min = 120.dp, max = 760.dp),
            isUser = isUser,
            messages = messages
        )

        if (isUser) {
            Spacer(Modifier.width(18.dp))

            ChatAvatar(
                painter = painterResource(Res.drawable.example_avatar),
                size = avatarSize
            )
        }
    }
}

@Composable
private fun ChatAvatar(
    painter: Painter,
    size: Int
) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = dawsonGold,
                shape = CircleShape
            ),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun ChatBubble(
    modifier: Modifier,
    isUser: Boolean,
    messages: List<Message>
) {
    val bubbleColor = if (isUser) {
        dawsonRed
    } else {
        cardColor
    }

    val bubbleBorderColor = if (isUser) {
        dangerColor.copy(alpha = 0.35f)
    } else {
        borderColor.copy(alpha = 0.9f)
    }

    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bubbleColor)
            .border(
                width = 1.dp,
                color = bubbleBorderColor,
                shape = shape
            )
            .padding(horizontal = 28.dp, vertical = 22.dp)
    ) {
        Column {
            SelectionContainer {
                Column {
                    messages.forEachIndexed { index, message ->
                        if (index > 0) {
                            Spacer(Modifier.height(8.dp))
                        }

                        MessageSegment(
                            message = message
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = messages.maxOfOrNull { it.createdTimestamp }?.toString() ?: "",
                    color = textSecondaryColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

//                if (isUser && message.delivered) {
//                    Spacer(Modifier.width(8.dp))
//
//                    Icon(
//                        imageVector = Icons.Outlined.Check,
//                        contentDescription = "Delivered",
//                        tint = infoColor,
//                        modifier = Modifier.size(18.dp)
//                    )
//                }
            }
        }
    }
}

@Composable
private fun MessageSegment(
    message: Message
) {
    val text = when (message.type) {
        TEXT_THINKING -> {
            "… (thinking…)".takeIf { message.chunks.isEmpty() } ?: message.chunks.entries.sortedBy { it.key }.joinToString("") { it.value }
        }
        else -> message.chunks.entries.sortedBy { it.key }.joinToString("") { it.value }
    }

    val fontWeight = when (message.type) {
        TEXT_PROMPT -> FontWeight.Medium
        TEXT_THINKING -> FontWeight.Normal
        TEXT_RESPONSE -> FontWeight.Medium
        TOOL_CALL_NAME -> FontWeight.Bold
        TOOL_CALL_RESULT -> FontWeight.SemiBold
        else -> FontWeight.Normal
    }

    val color = when (message.type) {
        TEXT_THINKING -> textSecondaryColor
        TOOL_CALL_NAME -> dawsonGold
        TOOL_CALL_RESULT -> textSecondaryColor
        else -> textPrimaryColor
    }

    Text(
        modifier = Modifier,
        text = text,
        fontSize = 22.sp,
        lineHeight = 34.sp,
        color = color,
        fontWeight = fontWeight,
    )
}

@Composable
fun UserInputBar(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onAttachClick: () -> Unit,
    onMicClick: () -> Unit,
    onSendClick: () -> Unit
) {
    val outerShape = RoundedCornerShape(24.dp)
    val inputShape = RoundedCornerShape(16.dp)
    val buttonShape = RoundedCornerShape(14.dp)
    val btnSize = 55
    val btnIconSize = 22
    val textFieldFont = 17

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(cardColor.copy(alpha = 0.55f))
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.75f),
                shape = outerShape
            )
            .padding(horizontal = 13.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserInputBtn(
                btnSize = btnSize,
                onClick = onAttachClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = "Attach file",
                    tint = textPrimaryColor,
                    modifier = Modifier.size(btnIconSize.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            UserInputBtn(
                btnSize = btnSize,
                onClick = onMicClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Voice input",
                    tint = textPrimaryColor,
                    modifier = Modifier.size(btnIconSize.dp)
                )
            }

            Spacer(Modifier.width(20.dp))

            BasicTextField(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = btnSize.dp, max = 160.dp)
                    .clip(inputShape)
                    .background(cardColor)
                    .border(
                        width = 1.dp,
                        color = borderColor.copy(alpha = 0.8f),
                        shape = inputShape
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = textPrimaryColor,
                    fontSize = textFieldFont.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(primaryColor),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onSendClick()
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                color = textSecondaryColor.copy(0.8f),
                                fontSize = textFieldFont.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        innerTextField()
                    }
                }
            )

            Spacer(Modifier.width(20.dp))

            Box(
                modifier = Modifier
                    .size(btnSize.dp)
                    .clip(buttonShape)
                    .background(dawsonRed)
                    .border(
                        width = 1.dp,
                        color = dangerColor.copy(alpha = 0.35f),
                        shape = buttonShape
                    )
                    .clickable(
                        enabled = value.isNotBlank(),
                        onClick = onSendClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Send",
                    tint = textPrimaryColor,
                    modifier = Modifier.size(btnIconSize.dp)
                )
            }
        }

        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = "Press Enter to send • Shift + Enter for new line",
            color = textSecondaryColor.copy(alpha = 0.75f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun UserInputBtn(
    btnSize: Int,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .size(btnSize.dp)
            .clip(shape)
            .background(cardColor)
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.85f),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}