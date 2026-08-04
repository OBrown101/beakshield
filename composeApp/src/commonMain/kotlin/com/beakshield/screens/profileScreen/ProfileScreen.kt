package com.beakshield.screens.profileScreen

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
import com.beakshield.viewModels.ProfileScreenViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileScreenViewModel: ProfileScreenViewModel,
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
        title = "Profile",
        subtitle = "This is your personal profile and kingdom overview.",
        destination = Destination.PROFILE
    ) {

    }
}