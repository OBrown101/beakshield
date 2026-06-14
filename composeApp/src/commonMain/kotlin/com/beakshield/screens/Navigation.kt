package com.beakshield.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.nav_btn_agents
import beakshield.composeapp.generated.resources.nav_btn_chats
import beakshield.composeapp.generated.resources.nav_btn_dawson
import beakshield.composeapp.generated.resources.nav_btn_decrees
import beakshield.composeapp.generated.resources.nav_btn_knowledge
import beakshield.composeapp.generated.resources.nav_btn_profile
import beakshield.composeapp.generated.resources.nav_btn_skills
import beakshield.composeapp.generated.resources.nav_btn_system
import com.beakshield.BeakShieldApp.agentsScreenViewModel
import com.beakshield.BeakShieldApp.chatsScreenViewModel
import com.beakshield.BeakShieldApp.decreesScreenViewModel
import com.beakshield.BeakShieldApp.knowledgeScreenViewModel
import com.beakshield.BeakShieldApp.mainScreenViewModel
import com.beakshield.BeakShieldApp.profileScreenViewModel
import com.beakshield.BeakShieldApp.skillsScreenViewModel
import com.beakshield.BeakShieldApp.systemScreenViewModel
import com.beakshield.dawsonRed
import com.beakshield.primaryColor
import com.beakshield.screens.chatsScreen.ChatsScreen
import com.beakshield.screens.mainScreen.MainScreen
import com.beakshield.screens.systemScreen.SystemScreen
import com.beakshield.viewModels.ChatsScreenViewModel
import com.beakshield.viewModels.MainScreenViewModel
import com.beakshield.viewModels.RailContent
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class Destination {
    MAIN,
    CHATS,
    AGENTS,
    KNOWLEDGE,
    DECREES,
    SKILLS,
    PROFILE,
    SYSTEM;

    val label: String
        get() = when(this) {
            MAIN -> "Dawson"
            CHATS -> "Chats"
            AGENTS -> "Agents"
            KNOWLEDGE -> "Knowledge"
            DECREES -> "Decrees"
            SKILLS -> "Skills"
            PROFILE -> "Profile"
            SYSTEM -> "System"
        }

    val icon: DrawableResource
        get() = when(this) {
            MAIN -> Res.drawable.nav_btn_dawson
            CHATS -> Res.drawable.nav_btn_chats
            AGENTS -> Res.drawable.nav_btn_agents
            KNOWLEDGE -> Res.drawable.nav_btn_knowledge
            DECREES -> Res.drawable.nav_btn_decrees
            SKILLS -> Res.drawable.nav_btn_skills
            PROFILE -> Res.drawable.nav_btn_profile
            SYSTEM -> Res.drawable.nav_btn_system
        }

    val railContent: StateFlow<RailContent?>
        get() = when (this) {
            MAIN -> mainScreenViewModel.railContent
            CHATS -> chatsScreenViewModel.railContent
            AGENTS -> agentsScreenViewModel.railContent
            KNOWLEDGE -> knowledgeScreenViewModel.railContent
            DECREES -> decreesScreenViewModel.railContent
            SKILLS -> skillsScreenViewModel.railContent
            PROFILE -> profileScreenViewModel.railContent
            SYSTEM -> systemScreenViewModel.railContent
        }
}

@Composable
fun AppNavHost(
    modifier: Modifier,
    navController: NavHostController,
    startDestination: Destination,
    navToScreen: (Destination) -> Unit,
    mainScreenViewModel: MainScreenViewModel,
    chatsScreenViewModel: ChatsScreenViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.name
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.name) {
                when (destination) {
                    Destination.MAIN -> MainScreen(
                        modifier = modifier,
                        mainScreenViewModel = mainScreenViewModel,
                        navToScreen = navToScreen
                    )
                    Destination.CHATS -> ChatsScreen(
                        modifier = modifier,
                        chatsScreenViewModel = chatsScreenViewModel,
                        navToScreen = navToScreen
                    )
                    Destination.AGENTS -> {}
                    Destination.KNOWLEDGE -> {}
                    Destination.DECREES -> {}
                    Destination.SKILLS -> {}
                    Destination.PROFILE -> {}
                    Destination.SYSTEM -> SystemScreen(
                        modifier = modifier,
                        systemScreenViewModel = systemScreenViewModel,
                        navToScreen = navToScreen
                    )
                }
            }
        }
    }
}

@Composable
fun NavButton(
    modifier: Modifier = Modifier,
    destination: Destination = Destination.MAIN,
    curDestination: Destination = Destination.MAIN,
    onClick: () -> Unit = {}
) {
    val borderModifier = if (curDestination == destination) {
        Modifier.border(
            BorderStroke(1.dp, primaryColor),
            RoundedCornerShape(12.dp)
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(borderModifier)
            .fillMaxHeight()
            .background(if (curDestination == destination) dawsonRed else Color.Transparent)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(destination.icon),
                contentDescription = "",
                modifier = Modifier
                    .height(25.dp)
                    .width(45.dp)
                    .background(Color.Transparent),
                alignment = Alignment.CenterStart,
                colorFilter = ColorFilter.tint(if (curDestination == destination) primaryColor else Color.White),
                contentScale = ContentScale.Fit
            )
            Text(
                modifier = Modifier
                    .weight(1f),
                text = destination.label.uppercase(),
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = if (curDestination == destination) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Start,
                color = if (curDestination == destination) Color.White else Color.White.copy(0.8f)
            )
        }
    }
}

@Preview
@Composable
fun NavigationRail(
    modifier: Modifier = Modifier,
    curDestination: Destination = Destination.MAIN,
    navToScreen: (Destination) -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
//     val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
//                .verticalScroll(scrollState)
                .background(Color.Transparent),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Destination.entries.forEach { destination ->
                Box(
                    modifier = Modifier
                ) {
                    NavButton(
                        modifier = Modifier
                            .height(80.dp)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        destination = destination,
                        curDestination = curDestination,
                        onClick = { navToScreen(destination) }
                    )
                }
            }
        }
    }
}