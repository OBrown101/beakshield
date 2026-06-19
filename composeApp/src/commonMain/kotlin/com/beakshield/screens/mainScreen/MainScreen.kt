package com.beakshield.screens.mainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.beakshield.BeakShieldApp.chatsScreenViewModel
import com.beakshield.screens.Destination
import com.beakshield.viewModels.MainScreenViewModel


@Composable
fun MainScreen(
    modifier: Modifier,
    mainScreenViewModel: MainScreenViewModel,
    navToScreen: (Destination) -> Unit
) {
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
                    navToScreen(Destination.CHATS)
                    chatsScreenViewModel.startNewChat()
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
    }
}