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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.beakshield.composables.BasicPasswordInputField
import com.beakshield.composables.BasicRoundedBtn
import com.beakshield.composables.BasicRoundedImageBtn
import com.beakshield.dawson.Server
import com.beakshield.dawsonGold
import com.beakshield.textColor
import com.beakshield.textSecondaryColor
import com.beakshield.websocket.ServerConnState

@Preview
@Composable
fun ServerConfigView(
    modifier: Modifier = Modifier,
    show: Boolean = true,
    server: Server? = Server.MockServer.mockServers[0],
    connState: ServerConnState = ServerConnState(),
    onConnect: (Server) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    var addressProvided by remember { mutableStateOf(server?.address ?: "") }
    var portProvided by remember { mutableStateOf(server?.port ?: 0) }
    var authProvided by remember { mutableStateOf(server?.authKey ?: "") }
    var fingerprintProvided by remember { mutableStateOf(server?.fingerprint ?: "") }
    val scrollState = rememberScrollState()
    val padBetween = 12

    if (!show) return

    fun connect() {
        val updatedServer = server?.copy(
            address = addressProvided,
            port = portProvided,
            authKey = authProvided,
            fingerprint = fingerprintProvided
        ) ?: Server(
            address = addressProvided,
            port = portProvided,
            authKey = authProvided,
            fingerprint = fingerprintProvided
        )
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
                    placeholder = "e.g. 192.168.1.100 or localhost",
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
                BasicPasswordInputField(
                    modifier = Modifier.padding(bottom = padBetween.dp),
                    label = "Auth Key",
                    titleFontSize = 14,
                    fontSize = 13,
                    value = server?.authKey ?: "",
                    placeholder = "SERVER_AUTH_KEY",
                    icon = Icons.Outlined.Key,
                    onValueChange = { value ->
                        authProvided = value
                    }
                )
                BasicPasswordInputField(
                    modifier = Modifier.padding(bottom = padBetween.dp),
                    label = "Fingerprint",
                    titleFontSize = 14,
                    fontSize = 13,
                    value = server?.fingerprint ?: "",
                    placeholder = "SERVER_FINGERPRINT",
                    icon = Icons.Outlined.Fingerprint,
                    onValueChange = { value ->
                        fingerprintProvided = value
                    }
                )
                StatusBox(
                    modifier = Modifier.padding(bottom = padBetween.dp),
                    connState = connState
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
    connState: ServerConnState
) {
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
                    .background(connState.color, CircleShape)
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(connState.color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)) { append("${connState.message}\n") }
                    withStyle(style = SpanStyle(textSecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Normal)) { append(connState.description) }
                },
                lineHeight = 17.sp
            )
        }
    }
}