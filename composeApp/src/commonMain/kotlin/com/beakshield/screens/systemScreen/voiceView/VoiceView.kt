package com.beakshield.screens.systemScreen.voiceView

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.cardColor
import com.beakshield.composables.BasicRoundedIconBtn
import com.beakshield.dawsonGold
import com.beakshield.lightGreenColor
import com.beakshield.screens.systemScreen.SystemBox
import com.beakshield.textColor
import com.beakshield.textSecondaryColor
import com.beakshield.voice.VoiceModelHandler

@Composable
fun VoiceView(
    modifier: Modifier = Modifier,
    state: VoiceModelHandler.State,
    onEnable: () -> Unit
) {
    SystemBox(
        modifier = modifier.fillMaxWidth(),
        title = "Voice",
        iconVector = Icons.Outlined.RecordVoiceOver
    ) {
        when (state) {
            is VoiceModelHandler.State.NotDownloaded -> {
                Text(
                    text = "Give thy kingdom a voice. Downloads the speech models (~400 MB) from your DAWSON server; afterwards, voice runs fully on this device.",
                    color = textSecondaryColor,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(16.dp))
                BasicRoundedIconBtn(
                    modifier = Modifier.width(180.dp).height(35.dp),
                    text = "Enable Voice",
                    textStyle = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        textAlign = TextAlign.Left
                    ),
                    imageVector = Icons.Outlined.RecordVoiceOver,
                    imageHeight = 16,
                    color = Color.Black,
                    borderRadius = 7,
                    borderColor = dawsonGold,
                    bg = dawsonGold,
                    onClick = onEnable
                )
            }

            is VoiceModelHandler.State.Downloading -> {
                Text(
                    text = "Downloading voice models…",
                    color = textColor,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = dawsonGold,
                    trackColor = cardColor
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = state.currentFile,
                        color = textSecondaryColor,
                        fontSize = 9.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "${(state.progress * 100).toInt()}%",
                        color = dawsonGold,
                        fontSize = 9.5.sp
                    )
                }
            }

            is VoiceModelHandler.State.Ready -> {
                Text(
                    text = "Voice is ready. Dawson speaks with his own voice; Squirebots report in theirs. Use the microphone in any chat, and the speaker toggle to have responses read aloud.",
                    color = lightGreenColor,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            is VoiceModelHandler.State.Error -> {
                Text(
                    text = state.message,
                    color = com.beakshield.dangerColor,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(12.dp))
                BasicRoundedIconBtn(
                    modifier = Modifier.width(120.dp).height(35.dp),
                    text = "Retry",
                    textStyle = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        textAlign = TextAlign.Left
                    ),
                    imageVector = Icons.Outlined.RecordVoiceOver,
                    imageHeight = 16,
                    color = Color.White,
                    borderRadius = 7,
                    borderColor = Color.White,
                    bg = cardColor,
                    onClick = onEnable
                )
            }
        }
    }
}
