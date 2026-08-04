package com.beakshield.screens.skillsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.beakshield.screens.Destination
import com.beakshield.screens.baseScreen.HeaderScreen
import com.beakshield.viewModels.SkillsScreenViewModel

@Composable
fun SkillsScreen(
    modifier: Modifier = Modifier,
    skillsScreenViewModel: SkillsScreenViewModel,
    navToScreen: (Destination) -> Unit = {}
) {
    val userInputFocusReq = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    HeaderScreen(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        title = "Skills",
        subtitle = "The crafts and expertise available to your kingdom.",
        destination = Destination.SKILLS
    ) {

    }
}