package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.backgroundColor
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.composables.BasicBox
import com.beakshield.composables.BasicRoundedBtn
import com.beakshield.composables.MemoryMarkdown
import com.beakshield.dangerColor
import com.beakshield.classes.DataStyle
import com.beakshield.memory.Memory
import com.beakshield.textColor
import com.beakshield.textSecondaryColor
import com.beakshield.websocket.memory.MemoryDrawer
import org.jetbrains.compose.resources.painterResource

@Preview(device = TABLET)
@Composable
fun DrawerDetailView(
    modifier: Modifier = Modifier,
    drawer: MemoryDrawer = MemoryDrawer.MockMemoryEntry.mockEntries.first(),
    isLoadingFull: Boolean = false,
    isDeleting: Boolean = false,
    deleteError: String? = null,
    onWingClick: (String) -> Unit = {},
    onRoomClick: (String, String) -> Unit = {_, _ ->},
    onDelete: (MemoryDrawer) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val padBetween = 12
    val wingStyle = DataStyle.styleFor(drawer.wing)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.35f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    onDismiss()
                })
            }
    )
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BasicBox(
            modifier = modifier
                .width(620.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                DetailHeader(
                    drawer = drawer,
                    wingStyle = wingStyle,
                    onDismiss = onDismiss
                )

                Spacer(Modifier.height(padBetween.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LocationButton(
                        label = DataStyle.displayName(drawer.wing).ifEmpty { "---" },
                        color = wingStyle.color,
                        enabled = drawer.wing.isNotEmpty(),
                        onClick = { onWingClick(drawer.wing) }
                    )
                    LocationButton(
                        label = drawer.room.ifEmpty { "---" },
                        color = wingStyle.color,
                        enabled = drawer.room.isNotEmpty(),
                        onClick = { onRoomClick(drawer.wing, drawer.room) }
                    )
                }

                Spacer(Modifier.height(padBetween.dp))
                DetailMetadata(drawer = drawer)

                Spacer(Modifier.height(padBetween.dp))
                Text(
                    text = if (isLoadingFull) "Memory Content (loading full entry...)" else "Memory Content",
                    color = textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .heightIn(min = 120.dp, max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor.copy(alpha = 0.55f))
                        .border(1.dp, borderColor.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                ) {
                    MemoryMarkdown(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        text = Memory.sanitizeMemoryContent(drawer.content.ifEmpty { "---" })
                    )
                }

                deleteError?.let { error ->
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = error,
                        color = dangerColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(Modifier.height((padBetween + 4).dp))
                BasicRoundedBtn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    text = if (isDeleting) "Deleting..." else "Delete Memory",
                    borderRadius = 8,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = dangerColor,
                        textAlign = TextAlign.Center
                    ),
                    color = dangerColor,
                    borderColor = dangerColor.copy(alpha = 0.5f),
                    bg = cardColor,
                    onClick = {
                        if (!isDeleting) {
                            onDelete(drawer)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailHeader(
    drawer: MemoryDrawer,
    wingStyle: DataStyle.Style,
    onDismiss: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(46.dp),
            painter = painterResource(wingStyle.emblem),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        SelectionContainer(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 14.dp),
                text = Memory.deriveAAAKTitle(drawer.content.lineSequence().firstOrNull()?.trim() ?: "")
                    ?: drawer.content.trim().take(Memory.DERIVED_TITLE_CHARS).ifEmpty { "Memory" },
                fontFamily = FontFamily.Serif,
                color = textColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .clickable { onDismiss() },
            imageVector = Icons.Outlined.Close,
            contentDescription = null,
            tint = textSecondaryColor
        )
    }
}

@Composable
private fun DetailMetadata(
    drawer: MemoryDrawer
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor.copy(alpha = 0.55f))
            .border(1.dp, borderColor.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        MetadataRow(
            label = "Drawer ID",
            value = drawer.id.ifEmpty { "---" }
        )
        MetadataRow(
            label = "Filed",
            value = drawer.filedAtDatetime ?: "---"
        )
        MetadataRow(
            label = "Agent",
            value = drawer.addedBy.ifEmpty { "---" }
        )
        MetadataRow(
            label = "Source",
            value = drawer.sourcePath.ifEmpty { drawer.sourceFile }.ifEmpty { "---" }
        )
        Memory.similarityLabel(drawer.similarity)?.let { similarity ->
            MetadataRow(label = "Match", value = similarity)
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.width(90.dp),
            text = label,
            color = textSecondaryColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal
        )
        SelectionContainer {
            Text(
                text = value,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LocationButton(
    label: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.6f)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}