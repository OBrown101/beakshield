package com.beakshield.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.cardColor
import com.beakshield.primaryColor

@Preview
@Composable
fun MessageBar(
    modifier: Modifier = Modifier,
    placeholderText: String = "Ask anything...",
    onSend: (String) -> Unit = {}
) {
    var text by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = (1.5).dp,
                color = primaryColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(cardColor.copy(0.9f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ChatBubbleOutline,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = primaryColor,
                    fontSize = 16.sp
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                modifier = Modifier.align(Alignment.CenterStart),
                                text = placeholderText,
                                textAlign = TextAlign.Start,
                                style = TextStyle(
                                    color = primaryColor.copy(alpha = 0.8f),
                                    fontSize = 16.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = primaryColor,
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 8.dp)
                    .clickable(enabled = text.isNotEmpty()) {
                        onSend(text)
                    }
            )
        }
    }
}