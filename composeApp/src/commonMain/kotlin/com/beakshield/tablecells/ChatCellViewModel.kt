package com.beakshield.tablecells

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.beakshield.composables.TableCellViewModel
import com.beakshield.dawson.Chat

data class ChatCellViewModel(
    override val id: Long,
    val chat: Chat,
    val onSelect: () -> Unit,
    val onDelete: () -> Unit
) : TableCellViewModel {
    override var selected by mutableStateOf(false)
    override var swipeState by mutableStateOf(TableCellViewModel.SwipeAnchor.Start)

    object MockMsgGroupCVM {
        val mockMsgGroupCVMs = listOf(
            ChatCellViewModel(
                id = 0L,
                chat = Chat.MockChat.mockChats[0],
                onSelect = {},
                onDelete = {}
            ),
            ChatCellViewModel(
                id = 1L,
                chat = Chat.MockChat.mockChats[1],
                onSelect = {},
                onDelete = {}
            ),
            ChatCellViewModel(
                id = 2L,
                chat = Chat.MockChat.mockChats[4],
                onSelect = {},
                onDelete = {}
            ),
            ChatCellViewModel(
                id = 3L,
                chat = Chat.MockChat.mockChats[5],
                onSelect = {},
                onDelete = {}
            )
        )
    }
}