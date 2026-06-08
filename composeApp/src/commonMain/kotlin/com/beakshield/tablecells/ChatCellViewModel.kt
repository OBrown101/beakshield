package com.beakshield.tablecells

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.beakshield.composables.TableCellViewModel
import com.beakshield.dawson.Chat

data class ChatCellViewModel(
    override val id: Long,
    val chat: Chat,
    val onSelect: () -> Unit
) : TableCellViewModel {
    override var selected by mutableStateOf(false)
    override var swipeState by mutableStateOf(TableCellViewModel.SwipeAnchor.Start)

    fun formattedLastTimestamp(lastTimestamp: Long?): String {
        return lastTimestamp.toString()
//        if (lastTimestamp == null || lastTimestamp == 0L) return ""
//
//        val zone = TimeZone.currentSystemDefault()
//        val now = Clock.System.now().toLocalDateTime(zone)
//        val time = Instant.fromEpochMilliseconds(lastTimestamp).toLocalDateTime(zone)
//        val daysBetween = time.date.daysUntil(now.date)
//
//        return when {
//            daysBetween == 0 -> {
//                time.time.toString().take(5) // HH:mm
//            }
//
//            daysBetween < 7 -> {
//                time.dayOfWeek.name
//                    .lowercase()
//                    .replaceFirstChar { it.uppercase() }
//                    .take(3) // Mon, Tue, etc.
//            }
//
//            else -> {
//                if (time.year == now.year) {
//                    "${time.month.number}/${time.day}"
//                } else {
//                    "${time.month.number}/${time.day}/${time.year}"
//                }
//            }
//        }
    }

    object MockMsgGroupCVM {
        val mockMsgGroupCVMs = listOf(
            ChatCellViewModel(
                id = 0L,
                chat = Chat.MockChat.mockChats[0],
                onSelect = {}
            ),
            ChatCellViewModel(
                id = 1L,
                chat = Chat.MockChat.mockChats[1],
                onSelect = {}
            ),
            ChatCellViewModel(
                id = 2L,
                chat = Chat.MockChat.mockChats[4],
                onSelect = {}
            ),
            ChatCellViewModel(
                id = 3L,
                chat = Chat.MockChat.mockChats[5],
                onSelect = {}
            )
        )
    }
}