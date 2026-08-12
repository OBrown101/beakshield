package com.beakshield.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.borderColor
import com.beakshield.dawsonGold
import com.beakshield.dawsonNavy
import com.beakshield.elevatedSurfaceColor
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
fun MainMarkdown(
    modifier: Modifier = Modifier,
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
                text = body,
                paragraph = body,
                ordered = body,
                bullet = body,
                list = body,
                code = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 17.sp, color = textPrimaryColor),
                inlineCode = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium, color = dawsonGold),
                h1 = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, color = textPrimaryColor, fontWeight = FontWeight.Bold),
                h2 = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, color = textPrimaryColor, fontWeight = FontWeight.Bold),
                h3 = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, color = textPrimaryColor, fontWeight = FontWeight.SemiBold),
                quote = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, color = textSecondaryColor, fontStyle = FontStyle.Italic)
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
        Icon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(13.dp)
                .clickable {
                    scope.launch {
                        clipboardManager.setText(AnnotatedString(rawForCopy))
                    }
                },
            imageVector = Icons.Outlined.ContentCopy,
            contentDescription = "Copy",
            tint = textSecondaryColor
        )
    }
}
