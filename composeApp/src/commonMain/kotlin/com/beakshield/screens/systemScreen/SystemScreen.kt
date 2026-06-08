package com.beakshield.screens.systemScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.BeakShieldApp.Companion.dawson
import com.beakshield.composables.BasicBox
import com.beakshield.dawsonGold
import com.beakshield.screens.Destination
import com.beakshield.screens.baseScreen.HeaderScreen
import com.beakshield.screens.systemScreen.providerViews.ProviderConfigView
import com.beakshield.screens.systemScreen.providerViews.ProvidersView
import com.beakshield.screens.systemScreen.serverView.ServerConfigView
import com.beakshield.screens.systemScreen.serverView.ServerView
import com.beakshield.textColor
import com.beakshield.viewModels.SystemScreenViewModel


@Composable
fun SystemScreen(
    modifier: Modifier,
    systemScreenViewModel: SystemScreenViewModel,
    navToScreen: (Destination) -> Unit
) {
    val currentServer = systemScreenViewModel.currentServer.collectAsState()
    val connectionState = dawson.connectionState.collectAsState()
    val providers by dawson.activeProviders.collectAsState()
    val providerTypeSelected by systemScreenViewModel.providerTypeSelected.collectAsState()
    val providerCellViewModels by systemScreenViewModel.providerCellViewModels.collectAsState()
    var showPopup by remember { mutableStateOf(false) }
    var showServerConfig by remember { mutableStateOf(false) }
    val padBetween = 12

    LaunchedEffect(showServerConfig, providerTypeSelected) {
        showPopup = (showServerConfig || (providerTypeSelected != null))
    }

    HeaderScreen(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        title = "System",
        subtitle = "Manage your kingdom's infrastructure, connections, and system settings."
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier
                        .padding(bottom = padBetween.dp)
                ) {
                    ServerView(
                        modifier = Modifier
                            .padding(end = padBetween.dp)
                            .weight(1f),
                        server = currentServer.value,
                        connected = connectionState.value,
                        onManage = {
                            showServerConfig = true
                        },
                        onDisconnect = { dawson.disconnect() }
                    )
                    ProvidersView(
                        modifier = Modifier.weight(1f),
                        providerCellViewModels = providerCellViewModels
                    )
                }
            }
            if (showPopup) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.7f))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {})
                        }
                )
            }
            ServerConfigView(
                modifier = Modifier
                    .width(425.dp),
                show = showServerConfig,
                server = currentServer.value,
                connected = connectionState.value,
                onTestConnection = {},
                onConnect = { systemScreenViewModel.connectToServer(it) },
                onCancel = {
                    showServerConfig = false
                }
            )
            ProviderConfigView(
                modifier = Modifier.width(375.dp),
                provider = providers.firstOrNull { it.type == providerTypeSelected },
                onSave = {
                    systemScreenViewModel.updateAPIKey(it)
                    systemScreenViewModel.selectProvider(null)
                },
                onCancel = {
                    systemScreenViewModel.selectProvider(null)
                }
            )
        }
    }
}

@Preview
@Composable
fun SystemBox(
    modifier: Modifier = Modifier,
    title: String = "DAWSON Server",
    iconVector: ImageVector = Icons.Outlined.Dns,
    content: @Composable () -> Unit = {}
) {
    BasicBox(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(28.dp),
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = dawsonGold
                )
                Text(
                    modifier = Modifier
                        .padding(start = 12.dp),
                    text = title,
                    fontFamily = FontFamily.Serif,
                    color = textColor,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            content()
        }
    }
}