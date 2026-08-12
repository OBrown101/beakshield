package com.beakshield.tablecells

import com.beakshield.classes.DataStyle
import com.beakshield.websocket.memory.MemoryCount

data class TopicCellViewModel(
    val id: Any,
    val wing: String,
    val room: MemoryCount,
    val onSelect: () -> Unit = {}
) {
    val displayName: String
        get() = DataStyle.displayName(room.name)

    val entryCount: Int
        get() = room.count

    val topicStyle: DataStyle.Style
        get() {
            val domainStyle = DataStyle.styleFor(wing)
            val roomStyle = DataStyle.styleFor(room.name)
            return DataStyle.Style(
                icon = roomStyle.icon,
                color = domainStyle.color,
                emblem = roomStyle.emblem
            )
        }

    object MockTopicCVM {
        val mockTopicCVMs = listOf(
            MemoryCount("decisions", 214),
            MemoryCount("conversations", 168),
            MemoryCount("patterns", 92),
            MemoryCount("diary", 61),
            MemoryCount("references", 33)
        ).mapIndexed { index, room ->
            TopicCellViewModel(id = index.toLong(), wing = "wing_android.development", room = room)
        }
    }
}