package com.beakshield.screens.mainScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.main_mascot_full
import com.beakshield.backgroundColor
import com.beakshield.cardColor
import com.beakshield.composables.BasicRoundedIconBtn
import com.beakshield.composables.MessageBar
import com.beakshield.dawsonDarkRed
import com.beakshield.dawsonRed
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainBg() {
    Box() {
        Image(
            modifier = Modifier
                .padding(top = 20.dp, end = 150.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .width(135.dp)
                .graphicsLayer {
                    colorFilter = ColorFilter.tint(Color.Black.copy(0.25f), BlendMode.Multiply)
                },
            painter = painterResource(Res.drawable.main_mascot_full),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopCenter
        )
    }
}

@Composable
fun MainHeader(
    modifier: Modifier = Modifier,
    title: String = "Good Evening, Ethan",
    statusText: String = "I've been monitoring your systems today\nEverything appears to be running smoothly."
) {
    val btnWidth = 250
    val btnHeight = 50
    val imageHeight = 25
    val btnTextSize = 15
    val btnSpacing = 10
    val padBetween = 12

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(100.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            backgroundColor.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier,
                    text = title,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    modifier = Modifier,
                    text = statusText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

            }
        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MessageBar(
                modifier = Modifier
                    .padding(horizontal = 35.dp)
                    .padding(top = padBetween.dp, bottom = padBetween.dp),
                placeholderText = "Ask Dawson anything..."
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(btnHeight.dp)
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(btnSpacing.dp)
            ) {
                BasicRoundedIconBtn(
                    modifier = Modifier.width(btnWidth.dp),
                    text = "Start Chat",
                    textSize = btnTextSize,
                    imageVector = Icons.Rounded.ChatBubble,
                    imageHeight = imageHeight,
                    color = Color.White,
                    borderColor = dawsonDarkRed,
                    bg = dawsonRed,
                    bgPressed = dawsonRed.copy(0.8f)
                )
                BasicRoundedIconBtn(
                    modifier = Modifier.width(btnWidth.dp),
                    text = "Talk to DAWSON",
                    textSize = btnTextSize,
                    imageVector = Icons.Rounded.ChatBubbleOutline,
                    imageHeight = imageHeight,
                    color = Color.White,
                    borderColor = backgroundColor,
                    bg = cardColor,
                    bgPressed = cardColor.copy(0.8f)
                )
                BasicRoundedIconBtn(
                    modifier = Modifier.width(btnWidth.dp),
                    text = "Start New Task",
                    textSize = btnTextSize,
                    imageVector = Icons.Rounded.Add,
                    imageHeight = imageHeight,
                    color = Color.White,
                    borderColor = backgroundColor,
                    bg = cardColor,
                    bgPressed = cardColor.copy(0.8f)
                )
            }
        }
    }
}