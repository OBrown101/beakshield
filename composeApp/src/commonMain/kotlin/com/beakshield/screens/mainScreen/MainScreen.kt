package com.beakshield.screens.mainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.beakshield.BeakShieldApp.chatsScreenViewModel
import com.beakshield.BeakShieldApp.dawson
import com.beakshield.screens.Destination
import com.beakshield.screens.systemScreen.LocalServerPopupView
import com.beakshield.viewModels.MainScreenViewModel
import com.beakshield.viewModels.SystemScreenViewModel
import com.beakshield.websocket.ServerConnState


@Composable
fun MainScreen(
    modifier: Modifier,
    mainScreenViewModel: MainScreenViewModel,
    systemScreenViewModel: SystemScreenViewModel,
    navToScreen: (Destination) -> Unit
) {
    val connectionState = dawson.connectionState.collectAsState()
    val installState by systemScreenViewModel.localInstallController.state.collectAsState()
    val isInstalled by systemScreenViewModel.localInstallController.isInstalled.collectAsState()
    val showAutoSetupView by systemScreenViewModel.showAutoSetupView.collectAsState()
    val localServerAvailable = ((connectionState.value.state == ServerConnState.ConnState.DISCONNECTED) &&
            systemScreenViewModel.localInstallController.isAvailable && !isInstalled)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.TopCenter
    ) {
        MainBg()
        Box(
            modifier = Modifier.padding(top = 265.dp)
        ) {
            MainHeader(
                modifier = Modifier,
                onStartChat = {
                    chatsScreenViewModel.startNewChat()
                    navToScreen(Destination.CHATS)
                },
                onDawsonChat = { prompt ->
                    chatsScreenViewModel.startPrimaryChat()
                    prompt?.let { chatsScreenViewModel.sendTextPrompt(it) }
                    navToScreen(Destination.CHATS)
                }
            )
            DashboardStatus(
                modifier = Modifier
                    .padding(top = 260.dp)
                    .padding(horizontal = 20.dp),
                onViewAgents = {},
                onViewDawsonTasks = {},
                onViewActivity = {}
            )
        }
        LocalServerPopupView(
            localServerAvailable = localServerAvailable,
            showAutoSetupView = showAutoSetupView,
            installState = installState
        )
    }
}