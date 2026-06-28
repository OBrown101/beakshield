package com.beakshield.notifications

import androidx.compose.ui.graphics.Color
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.dangerColor
import com.beakshield.dawsonDarkRed
import com.beakshield.primaryColor
import com.beakshield.primaryDarkColor
import com.beakshield.surfaceColor
import com.beakshield.textPrimaryColor

data class AlertButtonColors(
    val text: Color,
    val border: Color,
    val bg: Color,
    val bgPressed: Color
)

data class AlertButton(
    val text: String,
    val style: ButtonStyle = ButtonStyle.SECONDARY,
    val onClick: suspend () -> Unit = {}
) {

    enum class ButtonStyle {
        PRIMARY,
        SECONDARY,
        DANGER;

        val colors: AlertButtonColors
            get() = when (this) {
                PRIMARY -> AlertButtonColors(
                    text = backgroundColor,
                    border = primaryDarkColor,
                    bg = primaryColor,
                    bgPressed = primaryDarkColor
                )
                SECONDARY -> AlertButtonColors(
                    text = textPrimaryColor,
                    border = borderColor,
                    bg = Color.Transparent,
                    bgPressed = surfaceColor.copy(alpha = 0.55f)
                )
                DANGER -> AlertButtonColors(
                    text = Color.White,
                    border = dangerColor,
                    bg = dangerColor.copy(alpha = 0.85f),
                    bgPressed = dawsonDarkRed
                )
            }
    }
}