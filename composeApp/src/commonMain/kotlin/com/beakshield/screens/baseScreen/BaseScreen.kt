package com.beakshield.screens.baseScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import beakshield.composeapp.generated.resources.Res
import com.beakshield.screens.AppNavHost
import com.beakshield.screens.Destination
import com.beakshield.viewModels.BaseScreenViewModel
import com.beakshield.viewModels.MainScreenViewModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun BaseScreen(
    baseScreenViewModel: BaseScreenViewModel,
    mainScreenViewModel: MainScreenViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    fun navToScreen(destination: Destination) {
        navController.navigate(destination.name) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }
    }

    LaunchedEffect(navBackStackEntry) {
        // Called if current nav changes
    }

    MainBase() {
        AppNavHost(
            navController = navController,
            startDestination = Destination.MAIN,
            navToScreen = { navToScreen(it) },
            mainScreenViewModel = mainScreenViewModel
        )
    }
}

@PreviewScreenSizes
@Composable
private fun MainBasePreview() {
    MainBase()
}

@Composable
fun MainBase(
    content: @Composable () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) { innerPadding ->
//            Image(
//                painter = painterResource(Res.drawable.main_bg),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
            content()
        }
    }
}