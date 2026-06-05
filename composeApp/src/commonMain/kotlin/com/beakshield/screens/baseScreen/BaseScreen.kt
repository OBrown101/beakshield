package com.beakshield.screens.baseScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.main_bg
import beakshield.composeapp.generated.resources.nav_insignia
import com.beakshield.backgroundColor
import com.beakshield.screens.AppNavHost
import com.beakshield.screens.Destination
import com.beakshield.screens.NavigationRail
import com.beakshield.surfaceColor
import com.beakshield.viewModels.BaseScreenViewModel
import com.beakshield.viewModels.ChatsScreenViewModel
import com.beakshield.viewModels.MainScreenViewModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun BaseScreen(
    baseScreenViewModel: BaseScreenViewModel,
    mainScreenViewModel: MainScreenViewModel,
    chatsScreenViewModel: ChatsScreenViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val curDestination = navBackStackEntry?.destination?.route?.let { Destination.valueOf(it) } ?: Destination.MAIN
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

    MainBase(
        curDestination = curDestination,
        navToScreen = { navToScreen(it) }
    ) { modifier ->
        AppNavHost(
            modifier = modifier,
            navController = navController,
            startDestination = Destination.MAIN,
            navToScreen = { navToScreen(it) },
            mainScreenViewModel = mainScreenViewModel,
            chatsScreenViewModel = chatsScreenViewModel
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
    curDestination: Destination = Destination.MAIN,
    navToScreen: (Destination) -> Unit = {},
    content: @Composable (Modifier) -> Unit = {}
) {
    val navWidth = 210

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) { innerPadding ->
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopEnd),
                painter = painterResource(Res.drawable.main_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopEnd
            )
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(0.3f))
                    .fillMaxSize()
            )
            content(
                Modifier
                    .padding(start = navWidth.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(navWidth.dp)
                    .background(backgroundColor)
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = surfaceColor,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokeWidth
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .height(800.dp)
                        .align(Alignment.TopStart)
                ) {
                    Image(
                        modifier = Modifier
                            .padding(top = 30.dp, bottom = 20.dp)
                            .height(80.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(Color.Transparent),
                        painter = painterResource(Res.drawable.nav_insignia),
                        contentDescription = "",
                        contentScale = ContentScale.FillHeight
                    )
                    NavigationRail(
                        modifier = Modifier,
                        curDestination = curDestination,
                        navToScreen = navToScreen
                    )
                }
            }
        }
    }
}