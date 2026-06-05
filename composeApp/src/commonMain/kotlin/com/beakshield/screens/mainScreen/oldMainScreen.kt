package com.beakshield.screens.mainScreen

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainScreenViewModel: MainScreenViewModel,
    navToScreen: (Destination) -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    var ipAddress by remember { mutableStateOf("localhost")}
    var requestResponse by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var autoScroll by remember { mutableStateOf(true) }
    val chats by dawson.activeChats.collectAsState()
    val userUUIDSelected by dawson.currentUserUUID.collectAsState()
    val chatUUIDSelected by mainScreenViewModel.chatUUIDSelected.collectAsState()
    val pendingInputRequests by mainScreenViewModel.pendingInputRequests.collectAsState()
    val groupedMessages by mainScreenViewModel.groupedMessages.collectAsState()
    val state = rememberLazyListState()
    val atBottom by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val lastIndex = groupedMessages.entries.size - 1

            lastVisibleIndex >= lastIndex - 1
        }
    }

    LaunchedEffect(atBottom) {
        autoScroll = atBottom
    }

    LaunchedEffect(groupedMessages) {
        if (autoScroll && groupedMessages.isNotEmpty()) {
            delay(10)
            state.scrollToItem(groupedMessages.entries.size - 1)
        }
    }

    LaunchedEffect(chatUUIDSelected) {
        if (groupedMessages.isNotEmpty()) {
            autoScroll = true
            delay(10)
            state.scrollToItem(groupedMessages.entries.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(offBlackColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = chats.firstOrNull { it.uuid == chatUUIDSelected }?.uuid
                            ?: "Select Chat",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Chat") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        chats.forEach { chat ->
                            DropdownMenuItem(
                                text = { Text(chat.uuid) },
                                onClick = {
                                    mainScreenViewModel.selectChat(chat.uuid)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    modifier = Modifier.background(Color.White),
                    onClick = {
                        mainScreenViewModel.startPrimaryChat()
                    }
                ) {
                    Icon(Icons.Default.AddHome, contentDescription = "New Primary Chat")
                }
                IconButton(
                    modifier = Modifier.background(Color.White),
                    onClick = {
                        mainScreenViewModel.startNewChat()
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat")
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                state = state,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groupedMessages.entries.toList()) { entry ->
                    println("GROUP: ${entry.key}")

                    entry.value.forEach {
                        println(
                            "  source=${it.sourceUUID} " +
                                    "type=${it.type} " +
                                    "uuid=${it.uuid}"
                        )
                    }
                    ChatBubble(
                        entry.value,
                        userUUIDSelected
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 50.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter prompt...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                mainScreenViewModel.sendTextPrompt(userInput)
                                userInput = ""
                            }
                        },
                        enabled = userInput.isNotBlank()
                    ) {
                        Text("Send")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter ip address...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (ipAddress.isNotBlank()) {
                                println("Connect clicked")
                                mainScreenViewModel.setIPAddress(ipAddress)
                            }
                        },
                        enabled = ipAddress.isNotBlank()
                    ) {
                        Text("Connect")
                    }
                }
            }
        }
        pendingInputRequests.firstOrNull()?.let { request ->
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text("User Input Required")
                },
                text = {
                    Column {
                        Text(request.prompt)

                        if ((request.type != UserRequestType.PERMISSION) && (request.type != UserRequestType.CONFIRMATION)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextField(
                                value = requestResponse,
                                onValueChange = { requestResponse = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter response...") }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            dawson.respondToRequest(request, true, requestResponse.ifBlank { null })
                            requestResponse = ""
                        }
                    ) {
                        Text(
                            text = if (request.type == UserRequestType.PERMISSION) "Allow" else "Submit"
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            dawson.respondToRequest(request, false, null)
                            requestResponse = ""
                        }
                    ) {
                        Text(
                            text = if (request.type == UserRequestType.PERMISSION) "Deny" else "Cancel"
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ChatBubble(
    messages: List<Message>,
    userUUIDSelected: String?
) {
    val firstMsg = messages.firstOrNull()
    val isUser = (firstMsg?.sourceUUID == userUUIDSelected)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .background((if (isUser) mainColor else Color.LightGray), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            SelectionContainer {
                Column {
                    messages.forEachIndexed { idx, message ->
                        if (idx > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        ChatSegment(message, isUser)
                    }
                }
            }

            if (firstMsg != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "From: ${firstMsg.sourceUUID}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) Color.White else Color.Black,
                    modifier = Modifier.align(if (isUser) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}

@Composable
private fun ChatSegment(
    message: Message,
    isUser: Boolean
) {
    val style = when (message.type) {
        Message.MsgType.TEXT_PROMPT -> FontWeight.Normal
        Message.MsgType.TEXT_THINKING -> FontWeight.Light
        Message.MsgType.TEXT_RESPONSE -> FontWeight.Normal
        Message.MsgType.TOOL_CALL_NAME -> FontWeight.Bold
        Message.MsgType.TOOL_CALL_RESULT -> FontWeight.Bold
        Message.MsgType.DATA_PROMPT -> FontWeight.Medium
    }

    Text(
        modifier = Modifier
            .background(if (isUser) mainColor else Color.LightGray)
            .padding(8.dp),
        text = when (message.type) {
            Message.MsgType.TEXT_THINKING -> {
                "… (thinking…)".takeIf { message.chunks.isEmpty() } ?: message.chunks.entries.sortedBy { it.key }.joinToString("") { it.value }
            }
            else -> message.chunks.entries.sortedBy { it.key }.joinToString("") { it.value }
        },
        color = if (isUser) Color.White else Color.Black,
        fontWeight = style
    )
}
 */