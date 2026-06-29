package com.beakshield.screens.chatsScreen.chatView

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.composables.SquareRoundedIconBtn
import com.beakshield.composables.beakshieldScrollbar
import com.beakshield.dangerColor
import com.beakshield.dawson.Agent
import com.beakshield.dawson.Message
import com.beakshield.dawsonGold
import com.beakshield.dawsonRed
import com.beakshield.isJvm
import com.beakshield.pickFilePath
import com.beakshield.primaryColor
import com.beakshield.screens.chatsScreen.PendingInputRequestSegment
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor
import com.beakshield.websocket.UserInputRequest
import com.beakshield.websocket.UserInputResponse
import kotlinx.coroutines.launch


@Composable
fun ChatView(
    modifier: Modifier = Modifier,
    userInputFocusReq: FocusRequester,
    agent: Agent,
    groupedMessages: Map<String, List<Message>>,
    pendingInputRequests: List<UserInputRequest> = emptyList(),
    userUUIDSelected: String,
    onSendMessage: (String) -> Unit,
    onRetry: (String) -> Unit,
    onRespondToRequest: (UserInputResponse) -> Unit,
    onDeleteDirectory: (String) -> Unit = {},
    onAttachClick: (String) -> Unit = {},
    onMicClick: () -> Unit = {},
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pendingRequest = pendingInputRequests.firstOrNull { it.agentUUID == agent.uuid && it.userUUID == userUUIDSelected }
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    var stickToBottom by remember { mutableStateOf(true) }
    val messageScrollKey = groupedMessages.values
        .flatten()
        .sumOf { message ->
            message.chunks.values.sumOf { it.length }
        }

    fun attachDirectory() {
        scope.launch {
            val trimmed = userInput.text.trim()
            val path = if (isJvm) pickFilePath() else trimmed.takeIf { it.isNotEmpty() }
            path?.let {
                onAttachClick(it)
            }
        }
    }

    fun onSend() {
        val trimmed = userInput.text.trim()
        if (trimmed.isNotEmpty()) {
            pendingRequest?.let { request ->
                if (!request.type.textResp) return@let
                val response = UserInputResponse(agent.uuid, userUUIDSelected, null, trimmed)
                onRespondToRequest(response)
            } ?: run {
                onSendMessage(trimmed)
            }

            userInput = TextFieldValue("")
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@snapshotFlow false

            val lastItem = visibleItems.last()
            val totalItems = layoutInfo.totalItemsCount

            // User is at bottom if last item is visible and is the last item
            lastItem.index == totalItems - 1
        }.collect { atBottom ->
            stickToBottom = atBottom
        }
    }

    LaunchedEffect(agent.uuid) {
        // Scrolls to bottom if new chat selected
        val lastItem = groupedMessages.entries.toList().lastIndex
        if (lastItem >= 0) {
            listState.scrollToItem(lastItem)
            // Let layout settle before adjusting
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()?.let { item ->
                    val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
                    if (item.size > viewportHeight) {
                        val extraOffset = (item.size - viewportHeight).coerceAtLeast(0)
                        listState.scrollBy(extraOffset.toFloat())
                    }
                }
        }
        stickToBottom = true
    }

    LaunchedEffect(groupedMessages.keys.size) {
        // Only trigger on actual new message group, not during streaming
        if (stickToBottom) {
            val lastItem = groupedMessages.entries.toList().lastIndex
            if (lastItem >= 0) {
                listState.animateScrollToItem(lastItem)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier,
                state = listState,
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    items = groupedMessages.entries.toList(),
                    key = { (groupKey, _) -> groupKey }
                ) { (_, messages) ->
                    ChatBubbleRow(
                        state = agent.state,
                        isUser = (messages.firstOrNull()?.sourceUUID == userUUIDSelected),
                        messages = messages,
                        onRetry = onRetry
                    )
                }
            }
            ScrollBtn(
                modifier = Modifier.align(Alignment.BottomCenter),
                show = !stickToBottom,
                onClick = {
                    scope.launch {
                        val lastItem = groupedMessages.entries.toList().lastIndex
                        if (lastItem >= 0) {
                            listState.animateScrollToItem(lastItem)

                            listState.layoutInfo.visibleItemsInfo
                                .firstOrNull { it.index == lastItem }
                                ?.let { item ->
                                    val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
                                    val extraOffset = (item.size - viewportHeight).coerceAtLeast(0)
                                    listState.animateScrollBy(extraOffset.toFloat())
                                }
                            stickToBottom = true
                        }
                    }
                }
            )
        }

        pendingRequest?.let { request ->
            if (!request.type.binaryResp) return@let
            PendingInputRequestSegment(
                request = request,
                onApprove = {
                    val response = UserInputResponse(agent.uuid, userUUIDSelected, true, null)
                    onRespondToRequest(response)
                },
                onDeny = {
                    val response = UserInputResponse(agent.uuid, userUUIDSelected, false, null)
                    onRespondToRequest(response)
                }
            )

            Spacer(Modifier.height(10.dp))
        }

        UserInputBar(
            modifier = Modifier,
            userInputFocusReq = userInputFocusReq,
            value = userInput,
            onValueChange = { userInput = it },
            directories = agent.directories,
            awaitingResponse = agent.state.isAwaitingResponse,
            onRemoveDirectory = onDeleteDirectory,
            onAttachClick = {
                attachDirectory()
            },
            onMicClick = onMicClick,
            onSendClick = {
                onSend()
            },
            onCancel = onCancel
        )
    }
}

@Composable
private fun ScrollBtn(
    modifier: Modifier = Modifier,
    show: Boolean = true,
    onClick: () -> Unit = {}
) {
    if (!show) return

    val size = 40

    Box(
        modifier = modifier
    ) {
        IconButton(
            modifier = Modifier
                .size(size.dp),
            onClick = onClick
        ) {
            Icon(
                modifier = Modifier
                    .size((size - 15).dp)
                    .background(dawsonGold, CircleShape),
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun UserInputBar(
    modifier: Modifier = Modifier,
    userInputFocusReq: FocusRequester,
    value: TextFieldValue,
    directories: List<String>,
    awaitingResponse: Boolean,
    onRemoveDirectory: (String) -> Unit,
    onValueChange: (TextFieldValue) -> Unit,
    onAttachClick: () -> Unit,
    onMicClick: () -> Unit,
    onSendClick: () -> Unit,
    onCancel: () -> Unit,
) {

    val outerShape = RoundedCornerShape(24.dp)
    val inputShape = RoundedCornerShape(16.dp)
    val btnSize = 55
    val btnIconSize = 22
    val textFieldFont = 14

    fun handleClick(keyPress: Boolean = false) {
        if (awaitingResponse && keyPress) return
        if (awaitingResponse) {
            onCancel()
        } else {
            onSendClick()
        }
    }


    fun handleKeyEvent(event: KeyEvent): Boolean {
        return if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
            if (event.isShiftPressed) {
                val cursor = value.selection.start
                val newText = value.text.replaceRange(cursor, cursor, "\n")
                val tfValue = TextFieldValue(text = newText, selection = TextRange(cursor + 1))
                onValueChange(tfValue)
                true
            } else {
                handleClick(keyPress = true)
                true
            }
        } else {
            false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(cardColor.copy(alpha = 0.55f))
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.75f),
                shape = outerShape
            )
            .padding(horizontal = 13.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DirectoriesBox(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 96.dp)
                .padding(horizontal = 8.dp),
            directories = directories,
            onRemoveDirectory = onRemoveDirectory
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquareRoundedIconBtn(
                modifier = Modifier,
                btnSize = btnSize,
                bgColor = cardColor,
                borderColor = borderColor.copy(alpha = 0.85f),
                onClick = onAttachClick,
            ) {
                Icon(
                    modifier = Modifier.size(btnIconSize.dp),
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = "",
                    tint = textPrimaryColor
                )
            }
            Spacer(Modifier.width(12.dp))
            SquareRoundedIconBtn(
                modifier = Modifier,
                btnSize = btnSize,
                bgColor = cardColor,
                borderColor = borderColor.copy(alpha = 0.85f),
                enabled = false,    // TODO
                onClick = onMicClick,
            ) {
                Icon(
                    modifier = Modifier.size(btnIconSize.dp),
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Voice input",
                    tint = textPrimaryColor
                )
            }

            Spacer(Modifier.width(20.dp))
            BasicTextField(
                modifier = Modifier
                    .focusRequester(userInputFocusReq)
                    .weight(1f)
                    .heightIn(min = btnSize.dp, max = 160.dp)
                    .clip(inputShape)
                    .background(cardColor)
                    .border(
                        width = 1.dp,
                        color = borderColor.copy(alpha = 0.8f),
                        shape = inputShape
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .onPreviewKeyEvent {
                        handleKeyEvent(it)
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            userInputFocusReq.requestFocus()
                        })
                    },
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = textPrimaryColor,
                    fontSize = textFieldFont.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(primaryColor),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.text.isBlank()) {
                            Text(
                                text = "Ask agent anything...",
                                color = textSecondaryColor.copy(0.8f),
                                fontSize = textFieldFont.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        innerTextField()
                    }
                }
            )
            Spacer(Modifier.width(20.dp))

            SquareRoundedIconBtn(
                modifier = Modifier,
                btnSize = btnSize,
                bgColor = dawsonRed,
                borderColor = dangerColor.copy(alpha = 0.35f),
                enabled = value.text.isNotBlank() || awaitingResponse,
                onClick = {
                    handleClick()
                },
            ) {
                Icon(
                    modifier = Modifier.size(btnIconSize.dp),
                    imageVector = if (awaitingResponse) Icons.Outlined.Pause else Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "",
                    tint = textPrimaryColor
                )
            }
        }

        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = "Press Enter to send • Shift + Enter for new line",
            color = textSecondaryColor.copy(alpha = 0.75f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun DirectoriesBox(
    modifier: Modifier = Modifier,
    directories: List<String>,
    onRemoveDirectory: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val outerShape = RoundedCornerShape(24.dp)

    FlowRow(
        modifier = modifier
            .verticalScroll(scrollState)
            .beakshieldScrollbar(scrollState),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        directories.forEach { dir ->
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
                    .height(32.dp),
            ) {
                Row(
                    modifier = Modifier
                        .background(cardColor)
                        .border(
                            width = 1.dp,
                            color = borderColor.copy(alpha = 0.75f),
                            shape = outerShape
                        )
                        .padding(horizontal = 8.dp)
                        .wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(cardColor)
                            .size(20.dp),
                        onClick = { onRemoveDirectory(dir) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Remove",
                            tint = textPrimaryColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(cardColor)
                            .size(15.dp),
                        onClick = {
                            scope.launch {
                                clipboardManager.setText(AnnotatedString(dir))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = textPrimaryColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        modifier = Modifier,
                        text = dir,
                        color = textPrimaryColor,
                        fontSize = 12.sp,
                        overflow = TextOverflow.StartEllipsis,
                        textAlign = TextAlign.Start,
                        maxLines = 1
                    )
                }
            }
        }
    }
}