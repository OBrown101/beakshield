package com.beakshield.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.beakshield.backgroundColor
import com.beakshield.cardColor
import com.beakshield.dawsonGold

data class DropdownItem<T>(
    val value: T,
    val label: String,
    val icon: @Composable (() -> Unit)? = null
)

@Composable
fun <T> BubbleDropdown(
    modifier: Modifier = Modifier,
    items: List<DropdownItem<T>>,
    onItemSelected: (DropdownItem<T>) -> Unit = {},
    selectedItem: DropdownItem<T>? = null,
    menuWidth: Int = 260,
    menuHeight: Int? = null,
    triggerContent: @Composable (expanded: Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            triggerContent(expanded)
        }

        DropdownMenu(
            modifier = Modifier
                .width(menuWidth.dp)
                .then(menuHeight?.let { Modifier.heightIn(max = it.dp) } ?: Modifier)
                .clip(BubbleDropdownShape)
                .background(cardColor, BubbleDropdownShape)
                .border(1.dp, backgroundColor, BubbleDropdownShape),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = 8.dp),
            properties = PopupProperties(focusable = true),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            items.forEach { item ->
                val isSelected = selectedItem?.value == item.value

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            item.icon?.invoke()

                            Text(
                                modifier = Modifier.padding(start = 12.dp),
                                text = item.label,
                                color = if (isSelected) dawsonGold else Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

val BubbleDropdownShape: Shape = GenericShape { size, _ ->
    val radius = size.minDimension * 0.06f
    val bumpWidth = size.width * 0.10f
    val bumpHeight = size.height * 0.12f
    val bumpCenter = size.width * 0.88f

    moveTo(radius, 0f)

    lineTo(bumpCenter - bumpWidth, 0f)

    quadraticTo(bumpCenter - bumpWidth * 0.5f, 0f, bumpCenter, -bumpHeight)

    quadraticTo(bumpCenter + bumpWidth * 0.5f, 0f, bumpCenter + bumpWidth, 0f)
    lineTo(size.width - radius, 0f)
    quadraticTo(size.width, 0f, size.width, radius)
    lineTo(size.width, size.height - radius)
    quadraticTo(size.width, size.height, size.width - radius, size.height)
    lineTo(radius, size.height)
    quadraticTo(0f, size.height, 0f, size.height - radius)
    lineTo(0f, radius)
    quadraticTo(0f, 0f, radius, 0f)

    close()
}