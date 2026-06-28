package com.beakshield.screens.chatsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.beakshield.BeakShieldApp.dawson
import com.beakshield.screens.Destination
import com.beakshield.screens.chatsScreen.chatView.ChatView
import com.beakshield.viewModels.ChatsScreenViewModel

@Composable
fun ChatsScreen(
    modifier: Modifier,
    chatsScreenViewModel: ChatsScreenViewModel,
    navToScreen: (Destination) -> Unit
) {
    val userInputFocusReq = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val userUUIDSelected by dawson.currentUserUUID.collectAsState()
    val chatUUIDSelected by chatsScreenViewModel.chatUUIDSelected.collectAsState()
    val groupedMessages by chatsScreenViewModel.groupedMessages.collectAsState()
    val currentAgent by chatsScreenViewModel.currentAgent.collectAsState()
    val currentTitle by chatsScreenViewModel.currentTitle.collectAsState()
    val currentSubtitle by chatsScreenViewModel.currentSubtitle.collectAsState()
    val pendingRequests by dawson.pendingInputRequests.collectAsState()

    LaunchedEffect(currentAgent?.uuid) {
        userInputFocusReq.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        contentAlignment = Alignment.TopCenter
    ) {
        if (chatUUIDSelected != null) {
            Column() {
                ProfileView(
                    modifier = Modifier,
                    agent = currentAgent,
                    title = currentTitle,
                    subtitle = currentSubtitle,
                    onTitleChange = { chatsScreenViewModel.setTitle(it) },
                    onModeClick = { chatsScreenViewModel.setModeRequest(it) },
                    onModelClick = { chatsScreenViewModel.setModel(it) },
                    onThoughtClick = {},
                    onContextClick = {}
                )
                userUUIDSelected?.let { userUUID ->
                    currentAgent?.let { agent ->
                        ChatView(
                            modifier = Modifier,
                            userInputFocusReq = userInputFocusReq,
                            agent = agent,
                            groupedMessages = groupedMessages,
                            pendingInputRequests = pendingRequests,
                            userUUIDSelected = userUUID,
                            onSendMessage = { chatsScreenViewModel.sendTextPrompt(it) },
                            onRetry = { chatsScreenViewModel.retryPrompt(it) },
                            onRespondToRequest = { response ->
                                dawson.respondToRequest(response)
                            },
                            onDeleteDirectory = {
                                chatsScreenViewModel.removeDirectory(it)
                            },
                            onAttachClick = {
                                chatsScreenViewModel.addDirectories(it)
                            },
                            onMicClick = {},
                            onCancel = {
                                dawson.cancelAgentRun(agent.uuid)
                            }
                        )
                    }
                }
            }
        }
    }
}