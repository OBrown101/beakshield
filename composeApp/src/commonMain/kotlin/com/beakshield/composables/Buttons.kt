package com.beakshield.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.nav_btn_dawson
import com.beakshield.dangerColor
import com.beakshield.dawsonRed
import com.beakshield.textPrimaryColor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Preview
@Composable
fun SquareRoundedIconBtn(
    modifier: Modifier = Modifier,
    btnSize: Int = 55,
    bgColor: Color = dawsonRed,
    borderColor: Color = dangerColor,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {
        Icon(
            modifier = Modifier.size(22.dp),
            imageVector = Icons.Outlined.AttachFile,
            contentDescription = "",
            tint = textPrimaryColor
        )
    }
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .size(btnSize.dp)
            .clip(shape)
            .background(bgColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .alpha(if (enabled) 1f else 0.6f)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview
@Composable
fun BasicRoundedBtn(
    modifier: Modifier = Modifier,
    text: String = "Button",
    color: Color = Color.White,
    borderRadius: Int = 12,
    borderColor: Color = Color.Transparent,
    bg: Color = Color.Black,
    bgPressed: Color = bg.copy(0.8f),
    textStyle: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = color,
        textAlign = TextAlign.Left
    ),
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .border(
                BorderStroke(1.dp, borderColor),
                RoundedCornerShape(borderRadius.dp)
            )
            .clip(RoundedCornerShape(borderRadius.dp))
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
        Text(
            modifier = Modifier,
            text = text,
            style = textStyle
        )
    }
}

@Preview
@Composable
fun BasicRoundedIconBtn(
    modifier: Modifier = Modifier,
    text: String = "Button",
    imageVector: ImageVector? = Icons.Rounded.ChatBubble,
    imageHeight: Int = 20,
    color: Color = Color.White,
    borderRadius: Int = 12,
    borderColor: Color = Color.Transparent,
    bg: Color = Color.Black,
    bgPressed: Color = bg.copy(0.8f),
    textStyle: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = color,
        textAlign = TextAlign.Left
    ),
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .border(
                BorderStroke(1.dp, borderColor),
                RoundedCornerShape(borderRadius.dp)
            )
            .clip(RoundedCornerShape(borderRadius.dp))
            .background(if (pressed) bgPressed else bg)
            .fillMaxSize()
            .alpha(if (enabled) 1f else 0.6f)
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
            imageVector?.let { image ->
                Icon(
                    modifier = Modifier
                        .height(imageHeight.dp)
                        .padding(end = if (text.isNotEmpty()) 8.dp else 0.dp),
                    imageVector = image,
                    contentDescription = null,
                    tint = color
                )
            }
            if (text.isNotEmpty()) {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(end = imageVector?.let { 8.dp } ?: 0.dp),
                    text = text,
                    style = textStyle
                )
            }
        }
    }
}

@Preview
@Composable
fun BasicRoundedImageBtn(
    modifier: Modifier = Modifier,
    text: String = "Button",
    image: DrawableResource = Res.drawable.nav_btn_dawson,
    imageHeight: Int = 20,
    color: Color = Color.White,
    borderRadius: Int = 12,
    borderColor: Color = Color.Transparent,
    bg: Color = Color.Black,
    bgPressed: Color = bg.copy(0.8f),
    textStyle: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = color,
        textAlign = TextAlign.Left
    ),
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .border(
                BorderStroke(1.dp, borderColor),
                RoundedCornerShape(borderRadius.dp)
            )
            .clip(RoundedCornerShape(borderRadius.dp))
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
            Image(
                modifier = Modifier
                    .height(imageHeight.dp)
                    .padding(end = 8.dp),
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.Black)
            )
            if (text.isNotEmpty()) {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp),
                    text = text,
                    style = textStyle
                )
            }
        }
    }
}