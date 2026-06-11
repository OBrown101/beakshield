package com.beakshield.screens.systemScreen.providerViews

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.beakshield.cardColor
import com.beakshield.composables.BasicBox
import com.beakshield.composables.BasicInputField
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
    onSave: (String) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    var apiKeyProvided by remember { mutableStateOf(provider?.apiKey ?: "") }
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

    if (provider == null) return

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
                BasicInputField(
                    modifier = Modifier.padding(bottom = padBetween.dp),
                    label = "API Key",
                    titleFontSize = 14,
                    fontSize = 13,
                    value = provider.apiKey,
                    placeholder = "SECRET_API_KEY",
                    icon = Icons.Outlined.Key,
                    onValueChange = {
                        apiKeyProvided = it
                    }
                )
                Text(
                    text = "Models",
                    color = textPrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    modifier = Modifier
                        .padding(top = 5.dp, bottom = 10.dp)
                        .height(80.dp)
                        .fillMaxWidth()
                        .beakshieldScrollbar(scrollState)
                        .verticalScroll(scrollState),
                    text = provider.models.joinToString(separator = "\n", transform = { "• ${it.name}" }),
                    color = textPrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
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
                        onSave(apiKeyProvided)
                    }
                )
            }
        }
    }
}