package com.beakshield.tablecells

import com.beakshield.memory.WingStyle
import com.beakshield.websocket.memory.MemoryCount

data class TopicCellViewModel(
    val id: Any,
    val wing: String,
    val room: MemoryCount,
    val onSelect: () -> Unit = {}
) {
    val displayName: String
        get() = WingStyle.displayName(room.name)

    val entryCount: Int
        get() = room.count

    val topicStyle: WingStyle.Style
        get() {
            val domainStyle = WingStyle.styleFor(wing)
            val roomStyle = WingStyle.styleFor(room.name)
            return WingStyle.Style(
                icon = roomStyle.icon,
                color = domainStyle.color
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