package com.beakshield.screens.systemScreen.serverView

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.nav_btn_dawson
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.composables.BasicBox
import com.beakshield.composables.BasicInputField
import com.beakshield.composables.BasicRoundedBtn
import com.beakshield.composables.BasicRoundedImageBtn
import com.beakshield.dangerColor
import com.beakshield.dawson.Server
import com.beakshield.dawsonGold
import com.beakshield.lightGreenColor
import com.beakshield.textColor
import com.beakshield.textSecondaryColor

@Preview
@Composable
fun ServerConfigView(
    modifier: Modifier = Modifier,
    show: Boolean = true,
    server: Server? = Server.MockServer.mockServers[0],
    connected: Boolean = false,
    onTestConnection: () -> Unit = {},
    onConnect: (Server) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    var addressProvided by remember { mutableStateOf(server?.address ?: "") }
    var portProvided by remember { mutableStateOf(server?.port ?: 0) }
    val scrollState = rememberScrollState()
    val padBetween = 15

    if (!show) return

    fun connect() {
        val updatedServer = server?.copy(
            address = addressProvided,
            port = portProvided
        ) ?: Server(address = addressProvided, port = portProvided)
        onConnect(updatedServer)
    }

    BasicBox(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(cardColor, RoundedCornerShape(18.dp))
                .padding(24.dp)
        ) {
            HeaderBox(
                modifier = Modifier.padding(bottom = padBetween.dp),
                onCancel = onCancel
            )
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                BasicInputField(
                    modifier = Modifier.padding(bottom = padBetween.dp),
                    label = "Server Address",
                    titleFontSize = 14,
                    fontSize = 13,
                    value = server?.address ?: "",
                    placeholder = "e.g. 192.168.1.100 or dawson.local",
                    icon = Icons.Outlined.Dns,
                    onValueChange = {
                        addressProvided = it
                    }
                )
                BasicInputField(
                    modifier = Modifier.padding(bottom = padBetween.dp),
                    label = "Port",
                    titleFontSize = 14,
                    fontSize = 13,
                    value = server?.port?.toString() ?: "",
                    placeholder = "8080",
                    icon = Icons.Outlined.SettingsEthernet,
                    onValueChange = { value ->
                        value.toIntOrNull()?.let {
                            portProvided = it
                        }
                    }
                )
                StatusBox(
                    modifier = Modifier.padding(bottom = padBetween.dp),
                    connected = connected
                )
                TestConnectionBtn(
                    modifier = Modifier.padding(bottom = (padBetween + 10).dp),
                    onClick = onTestConnection
                )
                Buttons(
                    modifier = Modifier
                        .align(Alignment.End),
                    onConnect = { connect() },
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
private fun HeaderBox(
    modifier: Modifier,
    onCancel: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(50.dp)
                .padding(end = 18.dp),
            imageVector = Icons.Outlined.Security,
            contentDescription = null,
            tint = dawsonGold
        )

        Text(
            modifier = Modifier.weight(1f),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)) { append("Server Configuration\n") }
                withStyle(style = SpanStyle(textSecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Normal)) { append("Setup your DAWSON server connection.") }
            },
            lineHeight = 17.sp,
            fontFamily = FontFamily.Serif,
        )

        IconButton(
            modifier = Modifier,
            onClick = onCancel
        ) {
            Icon(
                modifier = Modifier.size(26.dp),
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close",
                tint = textSecondaryColor
            )
        }
    }
}

@Composable
private fun Buttons(
    modifier: Modifier = Modifier,
    onConnect: () -> Unit,
    onCancel: () -> Unit
) {
    val btnTextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = dawsonGold,
        textAlign = TextAlign.Center
    )
    val btnIconSize = 20
    val btnHeight = 40
    val btnRadius = 8

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicRoundedBtn(
            modifier = Modifier
                .height(btnHeight.dp)
                .width(80.dp),
            text = "Cancel",
            borderRadius = btnRadius,
            textStyle = btnTextStyle.copy(color = textColor),
            color = cardColor,
            borderColor = borderColor,
            bg = cardColor,
            onClick = onCancel
        )
        Spacer(modifier = Modifier.width(15.dp))
        BasicRoundedImageBtn(
            modifier = Modifier
                .height(btnHeight.dp)
                .width(100.dp),
            text = "Connect",
            borderRadius = btnRadius,
            textStyle = btnTextStyle.copy(color = Color.Black),
            image = Res.drawable.nav_btn_dawson,
            imageHeight = btnIconSize,
            color = dawsonGold,
            borderColor = dawsonGold,
            bg = dawsonGold,
            onClick = onConnect
        )
    }
}

@Composable
private fun StatusBox(
    modifier: Modifier = Modifier,
    connected: Boolean
) {
    val statusColor = if (connected) lightGreenColor else dangerColor
    val statusText = if (connected) "Connected" else "Not Connected"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .background(backgroundColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(statusColor, CircleShape)
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(statusColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)) { append("$statusText\n") }
                    withStyle(style = SpanStyle(textSecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Normal)) { append("Enter server address/port and test connection.") }
                },
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun TestConnectionBtn(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, dawsonGold.copy(alpha = 0.75f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent
        )
    ) {
        Icon(
            modifier = Modifier.size(25.dp),
            imageVector = Icons.Outlined.WifiTethering,
            contentDescription = null,
            tint = dawsonGold
        )
        Text(
            modifier = Modifier.padding(start = 10.dp),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(dawsonGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)) { append("Test Connection\n") }
                withStyle(style = SpanStyle(textSecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Normal)) { append("Check if the server is reachable.") }
            },
            lineHeight = 17.sp
        )
    }
}