package com.beakshield.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.primaryColor

@Preview
@Composable
fun BasicRoundedIconBtn(
    modifier: Modifier = Modifier,
    text: String = "Button",
    textSize: Int = 18,
    imageVector: ImageVector = Icons.Rounded.ChatBubble,
    imageHeight: Int = 20,
    color: Color = Color.White,
    borderColor: Color = Color.Transparent,
    bg: Color = Color.Black,
    bgPressed: Color = Color.Gray,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .border(
                BorderStroke(1.dp, borderColor),
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(if (pressed) bgPressed else bg)
            .fillMaxSize()
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 5.dp)
        ) {
            Icon(
                modifier = Modifier
                    .height(imageHeight.dp)
                    .padding(end = 8.dp),
                imageVector = imageVector,
                contentDescription = null,
                tint = color
            )
            if (text.isNotEmpty()) {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp),
                    text = text,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = textSize.sp,
                    fontWeight = FontWeight.Normal,
                    color = color,
                    textAlign = TextAlign.Left
                )
            }
        }
    }
}