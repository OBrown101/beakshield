package com.beakshield.screens.chatsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val chats by dawson.activeChats.collectAsState()
    val currentAgent by chatsScreenViewModel.currentAgent.collectAsState()
    val pendingRequests by dawson.pendingInputRequests.collectAsState()
    val scrollState = rememberScrollState()

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
                agent = currentAgent,
                title = chats.firstOrNull { it.uuid == chatUUIDSelected }?.title ?: "---",
                subtitle = chats.firstOrNull { it.uuid == chatUUIDSelected }?.subtitle ?: "---",
                onTitleChange = { chatsScreenViewModel.setTitle(it) },
                onModeClick = { chatsScreenViewModel.setMode(it) },
                onModelClick = { chatsScreenViewModel.setModel(it) },
                onContextClick = {}
            )
            userUUIDSelected?.let { userUUID ->
                currentAgent?.let { agent ->
                    ChatView(
                        modifier = Modifier.verticalScroll(scrollState),
                        agent = agent,
                        groupedMessages = groupedMessages,
                        pendingInputRequests = pendingRequests,
                        userUUIDSelected = userUUID,
                        onSendMessage = { chatsScreenViewModel.sendTextPrompt(it) },
                        onRespondToRequest = { response ->
                            dawson.respondToRequest(response)
                        },
                        onAttachClick = {
                            chatsScreenViewModel.addDirectories(it)
                        },
                        onMicClick = {}
                    )
                }
            }
        }
    }
}