package com.beakshield.screens.chatsScreen

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.user_profile
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.dangerColor
import com.beakshield.dawson.Agent
import com.beakshield.dawson.Message
import com.beakshield.dawson.Message.MsgType.DATA_PROMPT
import com.beakshield.dawson.Message.MsgType.TEXT_PROMPT
import com.beakshield.dawson.Message.MsgType.TEXT_RESPONSE
import com.beakshield.dawson.Message.MsgType.TEXT_THINKING
import com.beakshield.dawson.Message.MsgType.TOOL_CALL_NAME
import com.beakshield.dawson.Message.MsgType.TOOL_CALL_RESULT
import com.beakshield.dawsonGold
import com.beakshield.dawsonNavy
import com.beakshield.dawsonRed
import com.beakshield.elevatedSurfaceColor
import com.beakshield.isJvm
import com.beakshield.pickFilePath
import com.beakshield.primaryColor
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor
import com.beakshield.websocket.UserInputRequest
import com.beakshield.websocket.UserInputResponse
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


@Composable
fun ChatView(
    modifier: Modifier = Modifier,
    agent: Agent,
    groupedMessages: Map<String, List<Message>>,
    pendingInputRequests: List<UserInputRequest> = emptyList(),
    userUUIDSelected: String,
    onSendMessage: (String) -> Unit,
    onRespondToRequest: (UserInputResponse) -> Unit,
    onAttachClick: (String) -> Unit = {},
    onMicClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val pendingRequest = pendingInputRequests.firstOrNull { it.agentUUID == agent.uuid && it.userUUID == userUUIDSelected }
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var autoScrollEnabled by remember { mutableStateOf(true) }

    fun attachDirectory() {
        scope.launch {
            val path = if (isJvm) {
                pickFilePath()
            } else {
                userInput.trim().takeIf { it.isNotEmpty() }
            }

            path?.let {
                onAttachClick(it)
            }
        }
    }

    fun onSend() {
        val trimmed = userInput.trim()
        if (trimmed.isNotEmpty()) {
            pendingRequest?.let { request ->
                if (!request.type.textResp) return@let
                val response = UserInputResponse(agent.uuid, userUUIDSelected, null, trimmed)
                onRespondToRequest(response)
            } ?: run {
                onSendMessage(trimmed)
            }

            userInput = ""
        }
    }

    fun isAtBottom(): Boolean {
        val layoutInfo = listState.layoutInfo
        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return true

        return lastVisibleItem.index >= layoutInfo.totalItemsCount - 1 &&
                lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset + 48
    }

    val contentVersion = groupedMessages.values.sumOf { messages ->
        messages.sumOf { message ->
            message.chunks.values.sumOf { it.length }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            autoScrollEnabled = isAtBottom()
        }
    }

    LaunchedEffect(contentVersion, groupedMessages.size) {
        if (autoScrollEnabled && groupedMessages.isNotEmpty()) {
            listState.scrollToItem(groupedMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(26.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(
                items = groupedMessages.entries.toList(),
                key = { it.key}
            ) { entry ->
                ChatBubbleRow(
                    agent = agent,
                    isUser = (entry.value.firstOrNull()?.sourceUUID == userUUIDSelected),
                    messages = entry.value
                )
            }
        }

        pendingRequest?.let { request ->
            if (!request.type.binaryResp) return@let
            PendingInputRequestSegment(
                request = request,
                onApprove = {
                    val response = UserInputResponse(agent.uuid, userUUIDSelected, true, null)
                    onRespondToRequest(response)
                },
                onDeny = {
                    val response = UserInputResponse(agent.uuid, userUUIDSelected, false, null)
                    onRespondToRequest(response)
                }
            )

            Spacer(Modifier.height(10.dp))
        }

        UserInputBar(
            value = userInput,
            onValueChange = { userInput = it },
            placeholder = "Ask agent anything...",
            onAttachClick = {
                attachDirectory()
            },
            onMicClick = onMicClick,
            onSendClick = {
                onSend()
            }
        )
    }
}

@Composable
private fun PendingInputRequestSegment(
    request: UserInputRequest,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(dawsonNavy)
            .border(
                width = 1.dp,
                color = dawsonGold.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = request.type.name.lowercase().replaceFirstChar { it.uppercase() },
            color = dawsonGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = request.prompt,
            color = textPrimaryColor,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )

        if (request.type == UserInputRequest.ReqType.PERMISSION) {
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PendingRequestButton(
                    text = "Approve",
                    onClick = onApprove
                )

                PendingRequestButton(
                    text = "Deny",
                    onClick = onDeny
                )
            }
        }
    }
}

@Composable
private fun PendingRequestButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textPrimaryColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ChatBubbleRow(
    agent: Agent,
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
                painter = painterResource(agent.type.image),
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
                painter = painterResource(Res.drawable.user_profile),
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
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = dawsonGold,
                shape = CircleShape
            )
            .background(Color.Black),
        painter = painter,
        alignment = Alignment.Center,
        contentDescription = null,
        contentScale = ContentScale.Fit
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
                    fontSize = 11.sp,
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MessageSegment(
    message: Message
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val fontSize = 14
    val lineHeight = 17
    val text = when (message.type) {
        TEXT_THINKING -> {
            "… (thinking…)".takeIf { message.chunks.isEmpty() } ?: message.chunks.entries.sortedBy { it.key }.joinToString("") { it.value }
        }
        else -> message.chunks.entries.sortedBy { it.key }.joinToString("") { it.value }
    }

    val fontWeight = when (message.type) {
        TEXT_PROMPT -> FontWeight.Normal
        TEXT_THINKING -> FontWeight.Thin
        TEXT_RESPONSE -> FontWeight.Normal
        TOOL_CALL_NAME -> FontWeight.Thin
        TOOL_CALL_RESULT -> FontWeight.Thin
        else -> FontWeight.Normal
    }

    val color = when (message.type) {
        TEXT_THINKING -> textSecondaryColor
        TOOL_CALL_NAME -> dawsonGold
        TOOL_CALL_RESULT -> textSecondaryColor
        else -> textPrimaryColor
    }

    when (message.type) {
        TOOL_CALL_NAME, TOOL_CALL_RESULT, TEXT_PROMPT -> {
            Text(
                modifier = Modifier,
                text = text,
                fontSize = fontSize.sp,
                lineHeight = lineHeight.sp,
                color = color,
                fontWeight = fontWeight,
            )
        }
        DATA_PROMPT -> TODO()
        else -> {
            Box {
                Markdown(
                    modifier = Modifier.fillMaxWidth(),
                    content = text,
                    colors = markdownColor(
                        text = textPrimaryColor,
                        codeBackground = dawsonNavy,
                        inlineCodeBackground = dawsonNavy,
                        dividerColor = borderColor,
                        tableBackground = elevatedSurfaceColor
                    ),
                    typography = markdownTypography(
                        text = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = textPrimaryColor,
                            fontWeight = FontWeight.Normal
                        ),
                        code = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = textPrimaryColor
                        ),
                        inlineCode = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = dawsonGold
                        ),
                        h1 = TextStyle(
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            color = textPrimaryColor,
                            fontWeight = FontWeight.Bold
                        ),
                        h2 = TextStyle(
                            fontSize = 17.sp,
                            lineHeight = 23.sp,
                            color = textPrimaryColor,
                            fontWeight = FontWeight.Bold
                        ),
                        h3 = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = textPrimaryColor,
                            fontWeight = FontWeight.SemiBold
                        ),
                        quote = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = textSecondaryColor,
                            fontStyle = FontStyle.Italic
                        )
                    )
                )
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy",
                    tint = textSecondaryColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clickable {
                            scope.launch {
                                clipboardManager.setText(AnnotatedString(text))
                            }
                        }
                )
            }
        }
    }

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
    val textFieldFont = 14

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
                    lineHeight = 17.sp,
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