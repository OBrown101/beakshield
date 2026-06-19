package com.beakshield.screens.chatsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.composables.SquareRoundedIconBtn
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
import com.beakshield.formatTimestamp
import com.beakshield.infoColor
import com.beakshield.isJvm
import com.beakshield.pickFilePath
import com.beakshield.primaryColor
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor
import com.beakshield.websocket.UserInputRequest
import com.beakshield.websocket.UserInputResponse
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import kotlinx.coroutines.launch


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

    fun attachDirectory() {
        scope.launch {
            val path = if (isJvm) pickFilePath() else userInput.trim().takeIf { it.isNotEmpty() }
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(
                items = groupedMessages.entries.toList(),
                key = { (groupKey, _) -> groupKey }
            ) { (_, messages) ->
                ChatBubbleRow(
                    isUser = (messages.firstOrNull()?.sourceUUID == userUUIDSelected),
                    messages = messages
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
            modifier = Modifier,
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
private fun ChatBubbleRow(
    isUser: Boolean,
    messages: List<Message>
) {
    val rowPad = 40
    val paddingModifier = Modifier.padding(start = (if (isUser) rowPad.dp else 0.dp), end = (if (!isUser) rowPad.dp else 0.dp))

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.TopEnd else Alignment.TopStart,
    ) {
        ChatBubble(
            modifier = Modifier
                .then(paddingModifier),
            isUser = isUser,
            messages = messages
        )
    }
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
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            SelectionContainer {
                Column(
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
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
                modifier = Modifier.align(if (isUser) Alignment.Start else Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(messages.maxOfOrNull { it.createdTimestamp }) ?: "",
                    color = textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                if (isUser && messages.any { it.delivered }) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Delivered",
                        tint = infoColor
                    )
                }
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
    val text = message.chunks.entries.sortedBy { it.key }.joinToString("") { it.value }

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
        TEXT_THINKING -> {
            CollapsibleMessageSegment(
                title = message.type.label,
                text = text,
                fontSize = fontSize,
                lineHeight = lineHeight,
                color = textSecondaryColor
            )
        }
        TOOL_CALL_NAME, TOOL_CALL_RESULT -> {
            CollapsibleMessageSegment(
                title = message.type.label,
                text = text,
                fontSize = fontSize,
                lineHeight = lineHeight,
                color = color
            )
        }
        TEXT_PROMPT -> {
            CollapsibleBubbleContent {
                Text(
                    modifier = Modifier,
                    text = text,
                    fontSize = fontSize.sp,
                    lineHeight = lineHeight.sp,
                    color = color,
                    fontWeight = fontWeight,
                )
            }
        }
        DATA_PROMPT -> TODO()
        else -> {
            CollapsibleBubbleContent(
                enabled = !message.isStream
            ) {
                Box {
                    Markdown(
                        modifier = Modifier.padding(end = 18.dp),
                        content = text,
                        components = markdownComponents(
                            codeBlock = {
                                MarkdownHighlightedCodeBlock(
                                    content = it.content,
                                    node = it.node,
                                    showHeader = true
                                )
                            },
                            codeFence = {
                                MarkdownHighlightedCodeFence(
                                    content = it.content,
                                    node = it.node,
                                    showHeader = true
                                )
                            }
                        ),
                        colors = markdownColor(
                            text = textPrimaryColor,
                            codeBackground = dawsonNavy,
                            inlineCodeBackground = dawsonNavy,
                            dividerColor = borderColor,
                            tableBackground = elevatedSurfaceColor
                        ),
                        typography = markdownTypography(
                            text = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = textPrimaryColor,
                                fontWeight = FontWeight.Normal
                            ),
                            code = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                color = textPrimaryColor
                            ),
                            inlineCode = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = dawsonGold
                            ),
                            h1 = TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 24.sp,
                                color = textPrimaryColor,
                                fontWeight = FontWeight.Bold
                            ),
                            h2 = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                color = textPrimaryColor,
                                fontWeight = FontWeight.Bold
                            ),
                            h3 = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                color = textPrimaryColor,
                                fontWeight = FontWeight.SemiBold
                            ),
                            quote = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = textSecondaryColor,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(13.dp)
                            .clickable {
                                scope.launch {
                                    clipboardManager.setText(AnnotatedString(text))
                                }
                            },
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        tint = textSecondaryColor
                    )
                }
            }
        }
    }

}

@Composable
private fun CollapsibleMessageSegment(
    title: String,
    text: String,
    fontSize: Int,
    lineHeight: Int,
    color: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = color,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp),
                imageVector = if (expanded) {
                    Icons.Outlined.KeyboardArrowUp
                } else {
                    Icons.Outlined.KeyboardArrowDown
                },
                contentDescription = null,
                tint = color
            )
        }

        if (expanded) {
            Text(
                modifier = Modifier.padding(top = 4.dp, start = 8.dp),
                text = text,
                fontSize = fontSize.sp,
                lineHeight = lineHeight.sp,
                color = color,
                fontWeight = FontWeight.Thin
            )
        }
    }
}

@Composable
fun CollapsibleBubbleContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    collapsedHeight: Dp = 200.dp,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    /* Note: Tried using onGloballyPositioned, kept having jumpy behavior with scrolling main scrolling.
             AI recommended utilizing this (working) setup with SubcomposeLayout. */

    Box(modifier = modifier) {
        SubcomposeLayout(
            modifier = Modifier.clipToBounds()
        ) { constraints ->

            val collapsedHeightPx = collapsedHeight.roundToPx()

            val contentPlaceable = subcompose("content") {
                content()
            }.first().measure(constraints)

            val canCollapse = enabled && contentPlaceable.height > collapsedHeightPx

            val layoutHeight = if (!expanded && canCollapse) {
                collapsedHeightPx
            } else {
                contentPlaceable.height
            }

            layout(contentPlaceable.width, layoutHeight) {
                contentPlaceable.place(0, 0)
            }
        }

        SubcomposeLayout(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { constraints ->

            val collapsedHeightPx = collapsedHeight.roundToPx()

            val contentPlaceable = subcompose("measure") {
                content()
            }.first().measure(constraints)

            val canCollapse = enabled && contentPlaceable.height > collapsedHeightPx

            if (!canCollapse) {
                layout(0, 0) {}
            } else {
                val buttonPlaceable = subcompose("button") {
                    Box(
                        modifier = Modifier
                            .offset(y = 12.dp)
                            .background(
                                color = dawsonGold.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    ) {
                        IconButton(
                            modifier = Modifier.size(26.dp),
                            onClick = { expanded = !expanded }
                        ) {
                            Icon(
                                imageVector = if (expanded) {
                                    Icons.Default.Remove
                                } else {
                                    Icons.Default.Add
                                },
                                contentDescription = null
                            )
                        }
                    }
                }.first().measure(constraints)

                layout(buttonPlaceable.width, buttonPlaceable.height) {
                    buttonPlaceable.place(0, 0)
                }
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

            SquareRoundedIconBtn(
                modifier = Modifier,
                btnSize = btnSize,
                bgColor = cardColor,
                borderColor = borderColor.copy(alpha = 0.85f),
                onClick = onAttachClick,
            ) {
                Icon(
                    modifier = Modifier.size(btnIconSize.dp),
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = "",
                    tint = textPrimaryColor
                )
            }
            Spacer(Modifier.width(12.dp))
            SquareRoundedIconBtn(
                modifier = Modifier,
                btnSize = btnSize,
                bgColor = cardColor,
                borderColor = borderColor.copy(alpha = 0.85f),
                enabled = false,    // TODO
                onClick = onMicClick,
            ) {
                Icon(
                    modifier = Modifier.size(btnIconSize.dp),
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Voice input",
                    tint = textPrimaryColor
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

            SquareRoundedIconBtn(
                modifier = Modifier,
                btnSize = btnSize,
                bgColor = dawsonRed,
                borderColor = dangerColor.copy(alpha = 0.35f),
                enabled = value.isNotBlank(),
                onClick = onSendClick,
            ) {
                Icon(
                    modifier = Modifier.size(btnIconSize.dp),
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "",
                    tint = textPrimaryColor
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