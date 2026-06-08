package com.beakshield.screens.systemScreen.serverView

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.nav_btn_dawson
import com.beakshield.cardColor
import com.beakshield.composables.BasicRoundedIconBtn
import com.beakshield.composables.BasicRoundedImageBtn
import com.beakshield.dangerColor
import com.beakshield.dawson.Server
import com.beakshield.dawsonRed
import com.beakshield.lightGreenColor
import com.beakshield.screens.systemScreen.SystemBox
import com.beakshield.textColor

@Preview
@Composable
fun ServerView(
    modifier: Modifier = Modifier,
    server: Server? = Server.MockServer.mockServers[0],
    connected: Boolean = false,
    onManage: () -> Unit = {},
    onDisconnect: () -> Unit = {}
) {
    val btnTextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White,
        textAlign = TextAlign.Left
    )
    val btnIconHeight = 16
    val btnHeight = 35
    val btnRadius = 7

    SystemBox(
        modifier = modifier.fillMaxWidth(),
        title = "DAWSON Server",
        iconVector = Icons.Outlined.Dns
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .shadow(8.dp, CircleShape)
                    .background(
                        color = if (connected) lightGreenColor else dangerColor,
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        color = if (connected) lightGreenColor.copy(alpha = .18f) else dangerColor.copy(
                            alpha = .18f
                        ),
                        shape = CircleShape
                    )
            )
            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = if (connected) "Connected" else "Disconnected",
                    color = if (connected) lightGreenColor else dangerColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = if (connected) "Your kingdom is connected to DAWSON." else "Your kingdom is not connected to DAWSON.",
                    color = textColor.copy(0.8f),
                    fontSize = 9.5.sp
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        ServerInfoRow(
            label = "Server Address",
            value = server?.address ?: "---"
        )
        ServerInfoRow(
            label = "Port",
            value = server?.port?.toString() ?: "---"
        )
        ServerInfoRow(
            label = "Version",
            value = server?.version ?: "---"
        )
        ServerInfoRow(
            label = "Latency",
            value = "${server?.latencyMs ?: "---"} ms",
            valueColor = if (connected) lightGreenColor else textColor
        )
        ServerInfoRow(
            label = "Last Sync",
            value = server?.lastSyncTime?.toString() ?: "---",
            valueColor = if (connected) lightGreenColor else textColor
        )

        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(btnHeight.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicRoundedIconBtn(
                modifier = Modifier.weight(1f),
                text = "Manage Connection",
                textStyle = btnTextStyle,
                imageVector = Icons.Rounded.Settings,
                imageHeight = btnIconHeight,
                color = Color.White,
                borderRadius = btnRadius,
                borderColor = Color.White,
                bg = cardColor,
                onClick = onManage
            )
            BasicRoundedImageBtn(
                modifier = Modifier.weight(1f),
                text = "Disconnect",
                textStyle = btnTextStyle.copy(color = dangerColor),
                image = Res.drawable.nav_btn_dawson,
                imageHeight = btnIconHeight,
                color = dangerColor,
                borderRadius = btnRadius,
                borderColor = dangerColor,
                bg = dawsonRed.copy(0.6f),
                onClick = onDisconnect
            )
        }
    }
}

@Composable
private fun ServerInfoRow(
    label: String,
    value: String,
    valueColor: Color = textColor
) {
    val fontSize = 10
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = fontSize.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Medium
        )
    }
}