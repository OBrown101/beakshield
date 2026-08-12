package com.beakshield.tablecells

import com.beakshield.composables.TableCellViewModel
import com.beakshield.classes.DataStyle
import com.beakshield.websocket.memory.MemoryDrawer

data class KnowledgeCellViewModel(
    override val id: Any,
    val drawer: MemoryDrawer,
    var isNew: Boolean = false,
    val onSelect: () -> Unit = {},
    val onWingRoomClick: () -> Unit = {}
) : TableCellViewModel {
    override var selected: Boolean = false
    override var swipeState: TableCellViewModel.SwipeAnchor = TableCellViewModel.SwipeAnchor.Start

    val wingRoomLabel: String
        get() = "${DataStyle.displayName(drawer.wing)} \u203A ${drawer.room}"

    val wingStyle: DataStyle.Style
        get() = DataStyle.styleFor(drawer.wing)

    object MockKnowledgeCVM {
        val mockKnowledgeCVMs =
            MemoryDrawer.MockMemoryEntry.mockEntries.mapIndexed { index, drawer ->
                KnowledgeCellViewModel(
                    id = index.toLong(),
                    drawer = drawer,
                    isNew = (index < 3)
                )
            }
    }
}