package com.beakshield.screens.systemScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.BeakShieldApp.systemScreenViewModel
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.composables.BasicRoundedBtn
import com.beakshield.composables.BasicRoundedIconBtn
import com.beakshield.dangerColor
import com.beakshield.dawsonGold
import com.beakshield.dawsonRed
import com.beakshield.lightGreenColor
import com.beakshield.server.LocalDawsonInstallController.InstallState
import com.beakshield.textColor

@Composable
fun LocalServerPopupView(
    localServerAvailable: Boolean = true,
    showAutoSetupView: Boolean = true,
    installState: InstallState = InstallState.Idle
) {
    if (localServerAvailable && showAutoSetupView) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.35f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            LocalServerSetupView(
                state = installState,
                onSetUp = { systemScreenViewModel.localInstallController.setUp() },
                onCancel = { systemScreenViewModel.showAutoSetupView(false) }
            )
        }
    }
}

@Preview
@Composable
fun LocalServerSetupView(
    modifier: Modifier = Modifier,
    state: InstallState = InstallState.Idle,
    onSetUp: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val btnTextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White,
        textAlign = TextAlign.Left
    )
    val btnRadius = 8

    SystemBox(
        modifier = modifier.width(450.dp),
        title = "Run DAWSON on this Computer?",
        iconVector = Icons.Outlined.Dns
    ) {
        Text(
            text = "Beakshield can install and start the DAWSON server for you.\n\n" +
                    "1. Drag the downloaded DAWSON folder into your Home folder (user root).\n\n" +
                    "2. Press Set Up DAWSON below.",
            color = textColor.copy(0.8f),
            fontSize = (11.5).sp,
            lineHeight = 15.sp
        )

        Spacer(Modifier.height(16.dp))

        when (state) {
            is InstallState.Idle -> {
                Row(
                    modifier = Modifier
                        .height(35.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    BasicRoundedBtn(
                        modifier = Modifier
                            .width(95.dp),
                        text = "Cancel",
                        borderRadius = btnRadius,
                        textStyle = btnTextStyle.copy(color = textColor),
                        color = cardColor,
                        borderColor = borderColor,
                        bg = cardColor,
                        onClick = onCancel
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicRoundedIconBtn(
                        modifier = Modifier
                            .width(150.dp),
                        text = "Set Up DAWSON",
                        textStyle = btnTextStyle,
                        imageVector = Icons.Rounded.Bolt,
                        imageHeight = 16,
                        color = Color.White,
                        borderRadius = btnRadius,
                        borderColor = dangerColor.copy(alpha = 0.4f),
                        bg = dawsonRed,
                        onClick = onSetUp
                    )
                }
            }
            is InstallState.Working -> {
                CenteredRow {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = dawsonGold,
                        strokeWidth = 2.dp
                    )
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = state.step,
                        color = textColor.copy(0.85f),
                        fontSize = 11.sp
                    )
                }
            }
            is InstallState.Success -> {
                Text(
                    text = "DAWSON is running. Connecting your kingdom...",
                    color = lightGreenColor,
                    fontSize = 11.sp
                )
            }
            is InstallState.Failed -> {
                Text(
                    text = state.message,
                    color = dangerColor,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(10.dp))
                CenteredRow {
                    BasicRoundedIconBtn(
                        modifier = Modifier
                            .width(190.dp)
                            .height(35.dp),
                        text = "Try Again",
                        textStyle = btnTextStyle,
                        imageVector = Icons.Rounded.Bolt,
                        imageHeight = 16,
                        color = Color.White,
                        borderRadius = 7,
                        borderColor = dangerColor.copy(alpha = 0.4f),
                        bg = dawsonRed,
                        onClick = onSetUp
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}
