package com.beakshield

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.beakshield.screens.baseScreen.BaseScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        LaunchedEffect(Unit) {
            BeakShieldApp.onCreate()
        }

        BaseScreen(
            baseScreenViewModel = BeakShieldApp.baseScreenViewModel,
            mainScreenViewModel = BeakShieldApp.mainScreenViewModel,
            chatsScreenViewModel = BeakShieldApp.chatsScreenViewModel
        )
    }
}