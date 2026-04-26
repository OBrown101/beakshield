package com.beakshield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.painterResource

import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.compose_multiplatform
import com.beakshield.screens.baseScreen.BaseScreen
import com.beakshield.viewModels.BaseScreenViewModel
import com.beakshield.viewModels.MainScreenViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        BaseScreen(
            baseScreenViewModel = BeakShieldApp.baseScreenViewModel,
            mainScreenViewModel = BeakShieldApp.mainScreenViewModel
        )
    }
}