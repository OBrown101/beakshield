package com.beakshield.screens.systemScreen.providerViews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.cardDarkColor
import com.beakshield.composables.BasicBox
import com.beakshield.composables.BasicPasswordInputField
import com.beakshield.composables.BasicRoundedIconBtn
import com.beakshield.composables.beakshieldScrollbar
import com.beakshield.dawson.Provider
import com.beakshield.dawsonGold
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor


@Preview
@Composable
fun ProviderConfigView(
    modifier: Modifier = Modifier,
    provider: Provider? = Provider.MockProvider.mockProviders[0],
    onSave: (apiKey: String, useOAuth: Boolean, preferredModelIDs: List<String>, defaultModelID: String) -> Unit = { _, _, _, _ -> },
    onLogin: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    if (provider == null) return

    var apiKeyProvided by remember(provider.type) { mutableStateOf(provider.apiKey) }
    var useOAuth by remember(provider.type) { mutableStateOf(provider.useOAuth) }
    var defaultModelID by remember(provider.type) { mutableStateOf(provider.defaultModelID) }
    val preferredModelIDs = remember(provider.type) {
        mutableStateListOf<String>().apply { addAll(provider.preferredModelIDs) }
    }
    val scrollState = rememberScrollState()

    val btnTextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black,
        textAlign = TextAlign.Center
    )
    val btnIconSize = 20
    val btnHeight = 40
    val btnRadius = 8
    val padBetween = 15

    BasicBox(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(cardColor, RoundedCornerShape(18.dp))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = padBetween.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Config ${provider.type.label} Provider\n",
                    lineHeight = 17.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    modifier = Modifier,
                    onClick = onCancel
                ) {
                    Icon(
                        modifier = Modifier.size(26.dp),
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = textSecondaryColor
                    )
                }
            }
            Column(
                modifier = Modifier
            ) {
                if (provider.type.supportsOAuth) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = padBetween.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Use OAuth",
                            color = textPrimaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = useOAuth,
                            onCheckedChange = { useOAuth = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = dawsonGold,
                                checkedBorderColor = dawsonGold,
                                uncheckedThumbColor = textSecondaryColor,
                                uncheckedTrackColor = cardDarkColor,
                                uncheckedBorderColor = borderColor
                            )
                        )
                    }
                }
                if (useOAuth) {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = "If enabling OAuth, click save before logging in.",
                        color = textSecondaryColor,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    BasicRoundedIconBtn(
                        modifier = Modifier
                            .padding(bottom = padBetween.dp)
                            .height(btnHeight.dp)
                            .fillMaxWidth(),
                        text = "Login",
                        borderRadius = btnRadius,
                        textStyle = btnTextStyle,
                        imageVector = Icons.Outlined.AccountCircle,
                        imageHeight = btnIconSize,
                        color = Color.Black,
                        borderColor = dawsonGold,
                        bg = dawsonGold,
                        enabled = true,
                        onClick = onLogin
                    )
                } else {
                    BasicPasswordInputField(
                        modifier = Modifier.padding(bottom = padBetween.dp),
                        label = "API Key",
                        titleFontSize = 14,
                        fontSize = 13,
                        value = apiKeyProvided,
                        placeholder = "SECRET_API_KEY",
                        icon = Icons.Outlined.Key,
                        onValueChange = {
                            apiKeyProvided = it
                        }
                    )
                }
                Text(
                    text = "Models",
                    color = textPrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tap to toggle preferred • star sets default",
                    color = textSecondaryColor,
                    fontSize = 11.sp
                )
                Column(
                    modifier = Modifier
                        .padding(top = 5.dp, bottom = 10.dp)
                        .height(80.dp)
                        .fillMaxWidth()
                        .beakshieldScrollbar(scrollState)
                        .verticalScroll(scrollState)
                ) {
                    provider.availableModels.forEach { model ->
                        val isDefault = (model.id == defaultModelID)
                        val isPreferred = (isDefault || (model.id in preferredModelIDs))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (model.id in preferredModelIDs) {
                                        preferredModelIDs.remove(model.id)
                                        if (isDefault) {
                                            defaultModelID = ""
                                        }
                                    } else {
                                        preferredModelIDs.add(model.id)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = "• ${model.name}",
                                color = when {
                                    isDefault -> dawsonGold
                                    isPreferred -> textPrimaryColor
                                    else -> textSecondaryColor.copy(alpha = 0.5f)
                                },
                                fontSize = 14.sp,
                                fontWeight = if (isDefault) FontWeight.SemiBold else FontWeight.Normal
                            )
                            IconButton(
                                modifier = Modifier.size(24.dp),
                                onClick = {
                                    defaultModelID = if (isDefault) "" else model.id
                                    if (!isDefault && (model.id !in preferredModelIDs)) {
                                        preferredModelIDs.add(model.id)
                                    }
                                }
                            ) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    imageVector = if (isDefault) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = if (isDefault) "Default model" else "Set as default",
                                    tint = if (isDefault) dawsonGold else textSecondaryColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                BasicRoundedIconBtn(
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(btnHeight.dp)
                        .width(100.dp),
                    text = "Save",
                    borderRadius = btnRadius,
                    textStyle = btnTextStyle,
                    imageVector = Icons.Default.Save,
                    imageHeight = btnIconSize,
                    color = Color.Black,
                    borderColor = dawsonGold,
                    bg = dawsonGold,
                    enabled = true,
                    onClick = {
                        onSave(apiKeyProvided, useOAuth, preferredModelIDs.toList(), defaultModelID)
                    }
                )
            }
        }
    }
}