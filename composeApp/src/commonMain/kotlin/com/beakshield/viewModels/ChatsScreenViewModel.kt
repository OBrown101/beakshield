package com.beakshield.viewModels

import com.beakshield.BeakShieldApp.Companion.dawson
import com.beakshield.dawson.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatsScreenViewModel {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _chatUUIDSelected = MutableStateFlow<String?>(null)
    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatUUIDSelected = _chatUUIDSelected.asStateFlow()
    val allMessages = _chatMessages.asStateFlow()

    val pendingInputRequests = dawson.pendingInputRequests

    val groupedMessages: StateFlow<Map<String, List<Message>>> =
        allMessages.map { allMsgs ->
            allMsgs.groupBy { msg ->
                if (msg.isStream) "${msg.dataUUID}_${msg.sourceUUID}" else msg.uuid
            }.mapValues { (_, segs) ->
                segs.sortedBy { it.createdTimestamp }
            }
        }.stateIn(scope, SharingStarted.Lazily, emptyMap())

    init {
        scope.launch {
            _chatUUIDSelected.flatMapLatest { chatUUID ->
                if (chatUUID == null) {
                    flowOf(emptyList())
                } else {
                    dawson.activeChats.flatMapLatest { chats ->
                        chats.find { it.uuid == chatUUID }?.messages ?: flowOf(emptyList())
                    }
                }
            }.collect { messages ->
                _chatMessages.value = messages
            }
        }
    }
}