package com.beakshield.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.dawsonGold
import com.beakshield.primaryColor
import com.beakshield.textColor
import com.beakshield.textMutedColor
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor

@Preview
@Composable
fun BasicInputField(
    modifier: Modifier = Modifier,
    label: String = "",
    titleFontSize: Int = 14,
    fontSize: Int = 13,
    value: String = "",
    placeholder: String = "",
    icon: ImageVector = Icons.Default.FitnessCenter,
    onValueChange: (String) -> Unit = {}
) {
    var input by remember { mutableStateOf(value) }

    Column(modifier = modifier) {
        Text(
            text = label,
            color = textPrimaryColor,
            fontSize = titleFontSize.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
                .height(55.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = fontSize.sp),
            value = input,
            onValueChange = {
                input = it
                onValueChange(it)
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textSecondaryColor
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = textSecondaryColor,
                    fontSize = fontSize.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textPrimaryColor,
                unfocusedTextColor = textPrimaryColor,
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,
                cursorColor = dawsonGold
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Preview
@Composable
fun BasicOutlinedSearchBar(
    modifier: Modifier = Modifier,
    fontColor: Color = textMutedColor,
    placeholderText: String = "",
    value: String = "",
    onValueChange: (String) -> Unit = {}
) {
    var searchInput by remember { mutableStateOf("") }

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value.ifBlank { searchInput },
        onValueChange = {
            searchInput = it
            onValueChange(it)
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        placeholder = {
            Text(
                placeholderText,
                color = fontColor.copy(0.8f),
            )
        },
        trailingIcon = {
            Icon(
                Icons.Outlined.Search,
                null,
                tint = textMutedColor
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedContainerColor = cardColor,
            unfocusedContainerColor = cardColor
        ),
    )
}

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