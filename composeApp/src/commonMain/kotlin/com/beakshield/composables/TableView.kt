package com.beakshield.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


data class PreviewCellViewModel(
    override val id: Any,
    var title: String
) : TableCellViewModel {
    override var selected: Boolean = false
    override var swipeState: TableCellViewModel.SwipeAnchor =
        TableCellViewModel.SwipeAnchor.Start
}

@Preview(showBackground = true)
@Composable
fun TableViewPreview() {
    val items = remember {
        List(20) {
            PreviewCellViewModel(
                id = it,
                title = "Row $it"
            )
        }
    }
    TableView(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(Color(0xFF1E1E1E)),
        cellViewModels = items,
        tableHeight = 250.dp,
        cellHeight = { 50.dp },
        enableSwipeLeft = true,
        borderColor = Color.DarkGray,
        cellSpacing = 2,
        cellOnClick = {},
        cellOnSwipeLeft = {}
    ) { modifier, cell ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = cell.title,
                color = if (cell.selected) Color.Yellow else Color.White
            )
        }
    }
}

interface TableCellViewModel {
    val id: Any
    var selected: Boolean
    var swipeState: SwipeAnchor

    enum class SwipeAnchor {
        Start,
        Left,
    }
}

@Composable
fun <T: TableCellViewModel> TableViewCell(
    modifier: Modifier,
    cellHeight: (T) -> Dp,
    enableOnClick: Boolean,
    enableSwipeLeft: Boolean,
    borderColor: Color,
    cellViewModel: T,
    onClick: (T) -> Unit,
    onSwipeLeft: (T) -> Unit,
    content: @Composable (Modifier, T) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val swipeBtnSize = 75
    val animatedHeight by animateDpAsState(
        targetValue = cellHeight(cellViewModel),
        animationSpec = tween(200)
    )
    val swipeState = remember {
        AnchoredDraggableState(
            initialValue = cellViewModel.swipeState,
        ).apply {
            val newAnchors = DraggableAnchors {
                TableCellViewModel.SwipeAnchor.Start at with(density) { 0.dp.toPx() }
                TableCellViewModel.SwipeAnchor.Left at with(density) { -swipeBtnSize.dp.toPx() }
            }
            updateAnchors(newAnchors)
        }
    }

    LaunchedEffect(key1 = cellViewModel.swipeState) {
        swipeState.snapTo(cellViewModel.swipeState)
    }

    Box(
        modifier = modifier
            .drawWithContent {
                drawContent()
                val strokeWidth = 1.dp.toPx()
                drawLine(   // Draw bottom border
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .height(animatedHeight),
        contentAlignment = Alignment.Center
    ) {
        content(
            Modifier
                .anchoredDraggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                    enabled = enableSwipeLeft
                )
                .clickable(enabled = enableOnClick) {
                    cellViewModel.selected = !cellViewModel.selected
                    onClick(cellViewModel)
                }
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = swipeState.requireOffset().roundToInt(),
                        y = 0
                    )
                },
            cellViewModel
        )
        val swipePx = -swipeState.requireOffset().coerceAtMost(0f) // convert left drag to positive
        val swipeWidth = with(density) {
            ((swipePx / swipeBtnSize.dp.toPx()) * swipeBtnSize.dp.toPx()).coerceAtMost(swipeBtnSize.dp.toPx())
        }
        DeleteCellBtn(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(with(density) { swipeWidth.toDp() }),
            onClick = {
                cellViewModel.swipeState = TableCellViewModel.SwipeAnchor.Start
                scope.launch {
                    swipeState.animateTo(TableCellViewModel.SwipeAnchor.Start)
                    onSwipeLeft(cellViewModel)
                }
            }
        )
    }
}

@Composable
fun DeleteCellBtn(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                onClick()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = "DeleteOutline",
                tint = Color.White,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
fun <T : TableCellViewModel> TableView(
    modifier: Modifier = Modifier,
    cellViewModels: List<T>,
    tableHeight: Dp? = null,
    tableHeightMax: Dp? = null,
    tableHeightMin: Dp? = null,
    cellHeight: (T) -> Dp,
    emptyTableText: String = "",
    emptyTableFontSize: Int = 16,
    emptyTableTextColor: Color = Color.White,
    enableOnClick: Boolean = true,
    enableSwipeLeft: Boolean = false,
    borderColor: Color = Color.White,
    cellSpacing: Int = 0,
    cellOnClick: (T) -> Unit = {},
    cellOnSwipeLeft: (T) -> Unit = {},
    cellContent: @Composable (Modifier, T) -> Unit
) {
    val state = rememberLazyListState()

    Box(
        modifier = modifier
            .then(tableHeight?.let { Modifier.height(tableHeight) } ?: Modifier)
            .heightIn(max = tableHeightMax ?: Dp.Unspecified)
            .heightIn(min = tableHeightMin ?: Dp.Unspecified)
            .clipToBounds(),
        contentAlignment = Alignment.TopStart
    ) {
        LazyColumn(
            modifier = Modifier
                .beakshieldLazyScrollbar(state)
                .fillMaxSize(),
            state = state
        ) {
            items(
                items = cellViewModels,
                key = { it.id }
            ) { viewModel ->
                TableViewCell(
                    modifier = Modifier.padding(end = 8.dp),
                    cellHeight = cellHeight,
                    borderColor = borderColor,
                    cellViewModel = viewModel,
                    enableOnClick = enableOnClick,
                    enableSwipeLeft = enableSwipeLeft,
                    onClick = { cell ->
                            viewModel.selected = cell.selected
                            cellOnClick(cell)
                    },
                    onSwipeLeft = { cell ->
                        cellOnSwipeLeft(cell)
                    },
                    content = cellContent
                )
                Spacer(
                    modifier = Modifier.height(cellSpacing.dp)
                )
            }
        }
        if (emptyTableText.isNotEmpty() && cellViewModels.isEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(all = 15.dp)
                    .fillMaxWidth(),
                text = emptyTableText,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                fontSize = emptyTableFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = emptyTableTextColor
            )
        }
    }
}