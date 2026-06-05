package com.beakshield.screens.ChatsScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices.DESKTOP
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.beakshield.activeColor
import com.beakshield.backgroundColor
import com.beakshield.cardColor
import com.beakshield.dawson.Agent
import com.beakshield.dawsonGold
import org.jetbrains.compose.resources.painterResource

@Preview(device = DESKTOP)
@Composable
fun ProfileView(
    modifier: Modifier = Modifier,
    agent: Agent? = null,
    title: String = "Android Development Agent",
    onTitleChange: (String) -> Unit = {},
    onModeClick: () -> Unit = {},
    onContextClick: () -> Unit = {}
) {
    var modeDropdownExpanded by remember { mutableStateOf(false) }
    val padBetween = 10

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, dawsonGold, CircleShape),
        ) {
            agent?.let {
                Image(
                    modifier = Modifier,
                    painter = painterResource(it.type.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.width(28.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = padBetween.dp),
                value = title,
                onValueChange = onTitleChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Serif,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(2f, 3f),
                        blurRadius = 3f
                    )
                ),
                cursorBrush = SolidColor(dawsonGold),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            innerTextField()
                        }
                        Icon(
                            modifier = Modifier
                                .width(30.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit title",
                            tint = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            )

            Row(
                modifier = Modifier.padding(bottom = padBetween.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = dawsonGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "USBManager Refactor",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusCard(
                    icon = {
                        Box(
                            Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                        )
                    },
                    title = "Ready",
                    subtitle = "Agent is online",
                    clickable = false
                )
                Box() {
                    StatusCard(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = dawsonGold,
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        title = "Agent Capability",
                        subtitle = agent?.mode?.label ?: "Unknown",
                        onClick = {
                            modeDropdownExpanded = true
                            onModeClick()
                        }
                    )
                    ModeDropdown(
                        expanded = modeDropdownExpanded,
                        selectedMode = agent?.mode,
                        onDismissRequest = { modeDropdownExpanded = false },
                        onModeSelected = { selectedMode ->
                            // update your agent state here
                            println("Selected mode: ${selectedMode.label}")
                        }
                    )
                }
                StatusCard(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Layers,
                            contentDescription = null,
                            tint = dawsonGold,
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    title = "Context Window",
                    subtitle = "128K tokens",
                    onClick = onContextClick
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    clickable: Boolean = true,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .clip(shape)
            .background(cardColor.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
            .then(
                if (clickable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(15.dp))
        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(Color.White, fontSize = 13.sp)) { append(title + "\n") }
                    withStyle(style = SpanStyle(Color.White.copy(alpha = 0.65f), fontSize = 12.sp)) { append(subtitle) }
                },
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun ModeDropdown(
    expanded: Boolean,
    selectedMode: Agent.Mode?,
    onDismissRequest: () -> Unit,
    onModeSelected: (Agent.Mode) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
        modifier = modifier
            .clip(shape)
            .background(cardColor.copy(alpha = 0.97f))
            .border(
                width = 1.dp,
                color = cardColor.copy(1.5f),
                shape = shape
            )
    ) {
        Agent.Mode.entries.forEach { mode ->
            DropdownMenuItem(
                text = {
                    Row {
                        if (mode == selectedMode) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = dawsonGold
                            )
                        } else {
                            Spacer(Modifier.width(24.dp))
                        }

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = mode.label,
                            color = Color.White
                        )
                    }
                },
                onClick = {
                    onModeSelected(mode)
                    onDismissRequest()
                }
            )
        }
    }
}