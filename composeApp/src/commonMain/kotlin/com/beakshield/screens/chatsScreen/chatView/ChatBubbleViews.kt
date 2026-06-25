package com.beakshield.screens.chatsScreen.chatView

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
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
import com.beakshield.dangerColor
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
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import kotlinx.coroutines.launch

@Composable
fun ChatBubbleRow(
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
            CollapsibleBubbleContent(
                enabled = ((text.length > 1800) && (text.lines().size > 12))
            ) {
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
                enabled = ((text.length > 1800) && (text.lines().size > 12)) && !message.isStream
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
private fun CollapsibleBubbleContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    collapsedHeight: Dp = 200.dp,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .then(if (enabled && !expanded) Modifier.height(collapsedHeight) else Modifier)
            .clipToBounds()
    ) {
        Box(modifier = Modifier.padding(bottom = if (enabled) 20.dp else 0.dp)) {
            content()
        }
        if (enabled) {
            CollapseBtn(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                expanded = expanded,
                onClick = { expanded = !expanded }
            )
        }
    }
}

@Composable
private fun CollapseBtn(
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(225.dp)
                .height(3.dp)
                .background(dawsonGold.copy(alpha = 0.8f), RoundedCornerShape(5.dp))
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.Center)
                .size(20.dp),
            onClick = onClick
        ) {
            Icon(
                modifier = Modifier
                    .size(15.dp)
                    .background(dawsonGold, CircleShape),
                imageVector = if (expanded) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = null
            )
        }
    }
}