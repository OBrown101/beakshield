package com.beakshield.tablecells

import com.beakshield.composables.TableCellViewModel
import com.beakshield.formatRelativeTime
import com.beakshield.formatTimestamp
import com.beakshield.memory.Memory.DERIVED_TITLE_CHARS
import com.beakshield.memory.Memory.MAX_TITLE_CHARS
import com.beakshield.memory.Memory.deriveAAAKTitle
import com.beakshield.memory.Memory.parseDrawerTimestamp
import com.beakshield.memory.WingStyle
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

    val title: String
        get() {
            val firstLine = drawer.content.lineSequence().firstOrNull()?.trim() ?: ""
            val aaakTitle = deriveAAAKTitle(firstLine)
            return when {
                (aaakTitle != null) -> aaakTitle
                firstLine.isNotEmpty() && (firstLine.length <= MAX_TITLE_CHARS) -> firstLine
                else -> drawer.content.trim().take(DERIVED_TITLE_CHARS)
            }
        }

    val body: String?
        get() {
            val firstLine = drawer.content.lineSequence().firstOrNull()?.trim() ?: ""
            val trimmed = drawer.content.trim()
            return when {
                // AAAK/diary entries: everything after the first line reads as body;
                // the pipe-form first line is already summarized by the title.
                (deriveAAAKTitle(firstLine) != null) -> trimmed
                    .removePrefix(firstLine)
                    .trim()
                    .ifEmpty { null }

                else -> trimmed
                    .removePrefix(title)
                    .trim()
                    .ifEmpty { null }
            }
        }

    val wingRoomLabel: String
        get() = "${WingStyle.displayName(drawer.wing)} \u203A ${drawer.room}"

    val wingStyle: WingStyle.Style
        get() = WingStyle.styleFor(drawer.wing)

    val filedAtMillis: Long?
        get() = parseDrawerTimestamp(drawer.filedAt)

    val filedAtFormatted: String
        get() = formatTimestamp(filedAtMillis, true) ?: drawer.filedAt.take(10).ifEmpty { "---" }

    val filedAtFormattedRelative: String
        get() = formatRelativeTime(filedAtMillis) ?: drawer.filedAt.take(10).ifEmpty { "---" }

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