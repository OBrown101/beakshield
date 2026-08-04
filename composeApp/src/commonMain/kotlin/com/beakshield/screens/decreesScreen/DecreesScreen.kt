package com.beakshield.screens.decreesScreen

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
import com.beakshield.viewModels.DecreesScreenViewModel

@Composable
fun DecreesScreen(
    modifier: Modifier = Modifier,
    decreesScreenViewModel: DecreesScreenViewModel,
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
        title = "Decrees",
        subtitle = "The laws and standards that guide Dawson and the rest of your agents.",
        destination = Destination.DECREES
    ) {

    }
}