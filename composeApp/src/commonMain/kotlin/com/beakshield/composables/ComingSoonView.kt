package com.beakshield.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.backgroundColor
import com.beakshield.dawsonGold
import com.beakshield.textSecondaryColor

@Preview
@Composable
fun ComingSoonView(
    modifier: Modifier = Modifier,
    show: Boolean = true,
    title: String = "Coming Soon",
    subtitle: String = "This part of the kingdom is still under construction."
) {
    if (!show) return
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Outlined.Construction,
                contentDescription = null,
                tint = dawsonGold.copy(alpha = 0.7f)
            )
            Text(
                modifier = Modifier.padding(top = 14.dp),
                text = title,
                fontFamily = FontFamily.Serif,
                color = dawsonGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = subtitle,
                color = textSecondaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}