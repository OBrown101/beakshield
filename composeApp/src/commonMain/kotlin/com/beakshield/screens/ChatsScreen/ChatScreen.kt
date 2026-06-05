package com.beakshield.screens.ChatsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.beakshield.BeakShieldApp.Companion.dawson
import com.beakshield.screens.Destination
import com.beakshield.viewModels.ChatsScreenViewModel

@Composable
fun ChatsScreen(
    modifier: Modifier,
    chatsScreenViewModel: ChatsScreenViewModel,
    navToScreen: (Destination) -> Unit
) {
    val userUUIDSelected by dawson.currentUserUUID.collectAsState()
    val chatUUIDSelected by chatsScreenViewModel.chatUUIDSelected.collectAsState()
    val pendingInputRequests by chatsScreenViewModel.pendingInputRequests.collectAsState()
    val groupedMessages by chatsScreenViewModel.groupedMessages.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(

        ) {
            ProfileView(
                modifier = Modifier,
                agent = null,
                title = "Android Development Agent",
                onTitleChange = {},
                onModeClick = {},
                onContextClick = {}
            )
            userUUIDSelected?.let {
                ChatView(
                    modifier = Modifier,
                    groupedMessages = groupedMessages,
                    userUUIDSelected = it,
                    onSendMessage = {},
                    onAttachClick = {},
                    onMicClick = {}
                )
            }
        }
    }
}