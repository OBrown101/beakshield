package com.beakshield.tablecells

import com.beakshield.classes.DataStyle
import com.beakshield.websocket.memory.MemoryCount

data class DomainCellViewModel(
    val id: Any,
    val wing: MemoryCount,
    val onSelect: () -> Unit = {}
) {
    val displayName: String
        get() = DataStyle.displayName(wing.name)

    val entryCount: Int
        get() = wing.count

    val wingStyle: DataStyle.Style
        get() = DataStyle.styleFor(wing.name)

    object MockDomainCVM {
        val mockDomainCVMs = listOf(
            MemoryCount("wing_android.development", 542),
            MemoryCount("wing_research", 211),
            MemoryCount("wing_infrastructure", 182),
            MemoryCount("wing_writing", 47),
            MemoryCount("wing_user.preferences", 38)
        ).mapIndexed { index, wing ->
            DomainCellViewModel(id = index.toLong(), wing = wing)
        }
    }
}