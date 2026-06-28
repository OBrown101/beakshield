package com.beakshield.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.composables.BasicBox
import com.beakshield.composables.BasicRoundedBtn
import com.beakshield.dawsonNavy
import com.beakshield.infoColor
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor
import kotlinx.coroutines.launch
import org.legionarius.vector.notifications.AlertNotification


@Composable
fun AlertView(
    modifier: Modifier,
    currentAlert: AlertNotification?,
    onDismiss: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    currentAlert?.let { alert ->
        val icon = alert.icon.imageVector
        val iconColor = alert.icon.color

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.35f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {})
                }
        )
        BasicBox(
            modifier = modifier
                .wrapContentSize(),
            bgColor = dawsonNavy,
            borderColor = borderColor,
            borderRadius = 24
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .padding(horizontal = 35.dp, vertical = 35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(1.dp, iconColor.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(55.dp),
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))
                }

                Text(
                    text = alert.title,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor,
                        textAlign = TextAlign.Center
                    )
                )

                if (alert.message.isNotBlank()) {
                    Text(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 18.dp)
                            .padding(horizontal = 12.dp),
                        text = alert.message,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = textSecondaryColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )
                }

                alert.detailMessage?.takeIf { it.isNotBlank() }?.let { detailMessage ->
                    DetailMessageBox(
                        modifier = Modifier,
                        detailMessage = detailMessage
                    )
                }

                if (alert.buttons.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(top = 18.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(borderColor.copy(alpha = 0.65f))
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 18.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(34.dp)
                    ) {
                        alert.buttons.forEach { button ->
                            val colors = button.style.colors

                            BasicRoundedBtn(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(45.dp),
                                text = button.text,
                                color = colors.text,
                                borderRadius = 12,
                                borderColor = colors.border,
                                bg = colors.bg,
                                bgPressed = colors.bgPressed,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.text,
                                    textAlign = TextAlign.Center
                                ),
                                onClick = {
                                    onDismiss()
                                    scope.launch {
                                        button.onClick()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailMessageBox(
    modifier: Modifier,
    detailMessage: String
) {
    BasicBox(
        modifier = modifier,
        bgColor = backgroundColor.copy(alpha = 0.45f),
        borderColor = borderColor,
        borderRadius = 12
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = infoColor
            )
            Text(
                modifier = Modifier.padding(start = 15.dp),
                text = detailMessage,
                fontSize = 13.sp,
                color = infoColor,
                lineHeight = 18.sp
            )
        }
    }
}