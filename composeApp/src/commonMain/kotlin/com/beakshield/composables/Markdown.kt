package com.beakshield.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.BeakShieldApp.voice
import com.beakshield.borderColor
import com.beakshield.dawson.Agent
import com.beakshield.dawsonGold
import com.beakshield.dawsonNavy
import com.beakshield.elevatedSurfaceColor
import com.beakshield.mainColor
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainMarkdown(
    modifier: Modifier = Modifier,
    agentType: Agent.AgentType? = Agent.AgentType.DAWSON,
    text: String
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val body = TextStyle(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = textPrimaryColor,
        fontWeight = FontWeight.Normal
    )

    Box(
        modifier = modifier
    ) {
        Markdown(
            modifier = Modifier.padding(end = 43.dp),
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
                text = body,
                paragraph = body,
                ordered = body,
                bullet = body,
                list = body,
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
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
        ) {
            IconButton(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                clickedImageVector = Icons.AutoMirrored.Filled.VolumeUp,
                iconSize = 20,
                onClick = {
                    agentType?.let {
                        voice.speak(text, agentType = it)
                    }
                }
            )
            IconButton(
                imageVector = Icons.Outlined.ContentCopy,
                clickedImageVector = Icons.Filled.Check,
                iconSize = 15,
                onClick = {
                    scope.launch {
                        clipboardManager.setText(AnnotatedString(text))
                    }
                }
            )
        }
    }
}

@Composable
fun MemoryMarkdown(
    modifier: Modifier = Modifier,
    text: String,
    rawForCopy: String = text
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val body = TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = textPrimaryColor,
        fontWeight = FontWeight.Normal
    )
    val mono = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        color = textPrimaryColor
    )

    Box(
        modifier = modifier
    ) {
        Markdown(
            modifier = Modifier.padding(end = 18.dp),
            content = text,
            components = markdownComponents(
                codeBlock = {
                    MarkdownHighlightedCodeBlock(
                        content = it.content,
                        node = it.node,
                        showHeader = false
                    )
                },
                codeFence = {
                    MarkdownHighlightedCodeFence(
                        content = it.content,
                        node = it.node,
                        showHeader = false
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
                text = body,
                paragraph = body,
                ordered = body,
                bullet = body,
                list = body,
                code = mono,
                inlineCode = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium, color = dawsonGold),
                h1 = TextStyle(fontSize = 13.sp, lineHeight = 20.sp, color = dawsonGold, fontWeight = FontWeight.Bold),
                h2 = TextStyle(fontSize = 12.sp, lineHeight = 19.sp, color = dawsonGold, fontWeight = FontWeight.SemiBold),
                h3 = TextStyle(fontSize = 11.sp, lineHeight = 18.sp, color = textPrimaryColor, fontWeight = FontWeight.SemiBold),
                h4 = TextStyle(fontSize = 10.sp, lineHeight = 17.sp, color = textPrimaryColor, fontWeight = FontWeight.SemiBold),
                quote = TextStyle(fontSize = 10.sp, lineHeight = 17.sp, color = textSecondaryColor, fontStyle = FontStyle.Italic)
            )
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd),
            imageVector = Icons.Outlined.ContentCopy,
            onClick = {
                scope.launch {
                    clipboardManager.setText(AnnotatedString(rawForCopy))
                }
            }
        )
    }
}

@Composable
private fun IconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    clickedImageVector: ImageVector? = null,
    clickableSize: Int = 25,
    iconSize: Int = 15,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scope = rememberCoroutineScope()
    var isClickedTemporarily by remember { mutableStateOf(false) }

    val active = isPressed || isClickedTemporarily
    val iconTint = if (active) mainColor else textSecondaryColor
    val currentImageVector = if (isClickedTemporarily && clickedImageVector != null) {
        clickedImageVector
    } else {
        imageVector
    }

    Box(
        modifier = modifier
            .size(clickableSize.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    onClick()
                    if (clickedImageVector != null) {
                        scope.launch {
                            isClickedTemporarily = true
                            delay(2000)
                            isClickedTemporarily = false
                        }
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(iconSize.dp),
            imageVector = currentImageVector,
            contentDescription = "icon",
            tint = iconTint
        )
    }
}