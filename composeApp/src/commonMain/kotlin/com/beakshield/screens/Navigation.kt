package com.beakshield.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import beakshield.composeapp.generated.resources.Res
import com.beakshield.screens.mainScreen.MainScreen
import com.beakshield.viewModels.MainScreenViewModel

enum class Destination {
    MAIN;
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    navToScreen: (Destination) -> Unit,
    mainScreenViewModel: MainScreenViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.name
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.name) {
                when (destination) {
                    Destination.MAIN -> MainScreen(
                        mainScreenViewModel = mainScreenViewModel,
                        navToScreen = navToScreen
                    )
                }
            }
        }
    }
}