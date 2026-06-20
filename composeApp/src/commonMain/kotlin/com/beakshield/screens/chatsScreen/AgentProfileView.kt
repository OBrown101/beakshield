package com.beakshield.screens.chatsScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.BeakShieldApp.dawson
import com.beakshield.backgroundColor
import com.beakshield.cardColor
import com.beakshield.composables.BubbleDropdown
import com.beakshield.composables.DropdownItem
import com.beakshield.dangerColor
import com.beakshield.dawson.Agent
import com.beakshield.dawson.LLMModel
import com.beakshield.dawson.Provider
import com.beakshield.dawsonGold
import com.beakshield.formatWithSuffix
import com.beakshield.lightGreenColor
import org.jetbrains.compose.resources.painterResource

@Preview(device = TABLET)
@Composable
fun ProfileView(
    modifier: Modifier = Modifier,
    agent: Agent? = Agent.MockAgent.mockAgents[0],
    title: String? = "Android Development Agent",
    subtitle: String? = "USBManager Refactor",
    onTitleChange: (String) -> Unit = {},
    onModeClick: (Agent.Mode) -> Unit = {},
    onModelClick: (LLMModel) -> Unit = {},
    onContextClick: () -> Unit = {}
) {
    var titleProvided by remember { mutableStateOf(title) }
    val editMode = ((titleProvided != null) && (titleProvided != title))
    val titleIconModifier = if (editMode) {
        Modifier.padding(10.dp)
        Modifier.size(30.dp)
    } else {
        Modifier
    }
    val titleIconBgColor = when {
        (titleProvided == null) -> Color.Transparent
        editMode && (titleProvided?.isNotEmpty() ?: false) -> lightGreenColor
        editMode -> dangerColor
        else -> Color.Transparent
    }
    val padBetween = 10

    LaunchedEffect(title) {
        if ((titleProvided == null) || (titleProvided == "---") || (titleProvided == title)) {
            titleProvided = title
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(top = 20.dp, bottom = 15.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .border(2.dp, dawsonGold, CircleShape),
        ) {
            agent?.let {
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(it.type.image),
                    contentDescription = null,
                    contentScale = ContentScale.Fit
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
                value = titleProvided ?: "---",
                onValueChange = {
                    titleProvided = it
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 20.sp,
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
                                .size(22.dp)
                                .clickable(enabled = editMode) {
                                    val text = titleProvided ?: return@clickable
                                    if (text.isNotEmpty()) {
                                        onTitleChange(text)
                                    }
                                }
                                .clip(CircleShape)
                                .background(titleIconBgColor)
                                .then(titleIconModifier),
                            imageVector = if (editMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = "",
                            tint = Color.White
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
                    modifier = Modifier.width(275.dp),
                    text = subtitle?.ifBlank { null } ?: "---",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FlowRow(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                StatusCard(
                    icon = {
                        Box(
                            Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(agent?.state?.color ?: dawsonGold)
                        )
                    },
                    title = (agent?.state?.label ?: "Connecting"),
                    subtitle = agent?.state?.message ?: "Agent being spawned",
                    clickable = false
                )
                ModeDropdown(
                    modifier = Modifier,
                    agent = agent,
                    onChange = onModeClick
                )
                ModelDropdown(
                    modifier = Modifier,
                    agent = agent,
                    onChange = onModelClick
                )
                StatusCard(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Layers,
                            contentDescription = null,
                            tint = dawsonGold,
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    title = "Thought Window",
                    subtitle = agent?.thoughtWindow?.formatWithSuffix()?.let { "$it msgs" } ?: "---",
                    onClick = onContextClick
                )
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
                    subtitle = agent?.contextWindow?.formatWithSuffix()?.let { "$it tokens" } ?: "---",
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
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun ModeDropdown(
    modifier: Modifier,
    agent: Agent?,
    onChange: (Agent.Mode) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        val modeItems = Agent.Mode.entries.map { mode ->
            DropdownItem(
                value = mode,
                label = mode.label,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = dawsonGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            )
        }

        BubbleDropdown(
            selectedItem = modeItems.firstOrNull { it.value == agent?.mode },
            items = modeItems,
            menuWidth = 180,
            onItemSelected = { onChange(it.value) },
            triggerContent = {
                StatusCard(
                    icon = {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = dawsonGold
                        )
                    },
                    title = "Agent Capability",
                    subtitle = agent?.mode?.label ?: "---",
                    clickable = false
                )
            }
        )
    }
}

@Composable
fun ModelDropdown(
    modifier: Modifier,
    agent: Agent?,
    onChange: (LLMModel) -> Unit
) {
    val isPreview = LocalInspectionMode.current
    val providers by if (isPreview) remember { mutableStateOf(Provider.MockProvider.mockProviders) } else dawson.activeProviders.collectAsState()

    Box(
        modifier = modifier
    ) {
        val modelItems = providers.flatMap { it.models }.map { model ->
            DropdownItem(
                value = model,
                label = model.name,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = null,
                        tint = dawsonGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            )
        }

        BubbleDropdown(
            modifier = Modifier,
            selectedItem = modelItems.firstOrNull { it.value.name == agent?.model?.name },
            items = modelItems,
            menuHeight = 180,
            menuWidth = 180,
            onItemSelected = { onChange(it.value) },
            triggerContent = {
                StatusCard(
                    icon = {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            imageVector = Icons.Outlined.Book,
                            contentDescription = null,
                            tint = dawsonGold
                        )
                    },
                    title = "Model",
                    subtitle = agent?.model?.name ?: "---",
                    clickable = false
                )
            }
        )
    }
}