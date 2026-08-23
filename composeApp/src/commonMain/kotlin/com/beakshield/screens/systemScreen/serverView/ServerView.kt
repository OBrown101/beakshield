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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.rounded.Bolt
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
import com.beakshield.cardColor
import com.beakshield.composables.BasicRoundedIconBtn
import com.beakshield.dangerColor
import com.beakshield.dawson.Server
import com.beakshield.dawsonRed
import com.beakshield.screens.systemScreen.SystemBox
import com.beakshield.textColor
import com.beakshield.websocket.ServerConnState

@Preview
@Composable
fun ServerView(
    modifier: Modifier = Modifier,
    server: Server? = Server.MockServer.mockServers[0],
    connState: ServerConnState = ServerConnState(),
    localServerAvailable: Boolean = true,
    onManage: () -> Unit = {},
    onSetUpLocal: () -> Unit = {}
) {
    val serverText = when (connState.state) {
        ServerConnState.ConnState.CONNECTED -> "Your kingdom is connected to DAWSON."
        ServerConnState.ConnState.DISCONNECTED -> "Your kingdom is not connected to DAWSON."
        ServerConnState.ConnState.ERROR -> "Your kingdom is having difficulty connecting to DAWSON"
    }
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
                        color = connState.color,
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        color = connState.color.copy(alpha = .18f),
                        shape = CircleShape
                    )
            )
            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = connState.message,
                    color = connState.color,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = serverText,
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
            valueColor = connState.color
        )
        ServerInfoRow(
            label = "Last Sync",
            value = server?.lastSyncTime?.toString() ?: "---",
            valueColor = connState.color
        )
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(btnHeight.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (localServerAvailable) {
                BasicRoundedIconBtn(
                    modifier = Modifier
                        .width(150.dp),
                    text = "Setup on Computer",
                    textStyle = btnTextStyle,
                    imageVector = Icons.Rounded.Bolt,
                    imageHeight = 16,
                    color = Color.White,
                    borderRadius = btnRadius,
                    borderColor = dangerColor.copy(alpha = 0.4f),
                    bg = dawsonRed,
                    onClick = onSetUpLocal
                )
                Spacer(Modifier.width(10.dp))
            }
            BasicRoundedIconBtn(
                modifier = Modifier
                    .width(150.dp),
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