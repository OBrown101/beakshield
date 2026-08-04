package com.beakshield.screens.baseScreen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.main_bg
import beakshield.composeapp.generated.resources.nav_insignia
import com.beakshield.BuildInfo
import com.beakshield.backgroundColor
import com.beakshield.notifications.AlertNotification
import com.beakshield.notifications.AlertView
import com.beakshield.primaryColor
import com.beakshield.screens.AppNavHost
import com.beakshield.screens.Destination
import com.beakshield.screens.NavigationRail
import com.beakshield.surfaceColor
import com.beakshield.textColor
import com.beakshield.viewModels.AgentsScreenViewModel
import com.beakshield.viewModels.BaseScreenViewModel
import com.beakshield.viewModels.ChatsScreenViewModel
import com.beakshield.viewModels.DecreesScreenViewModel
import com.beakshield.viewModels.KnowledgeScreenViewModel
import com.beakshield.viewModels.MainScreenViewModel
import com.beakshield.viewModels.ProfileScreenViewModel
import com.beakshield.viewModels.SkillsScreenViewModel
import com.beakshield.viewModels.SystemScreenViewModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun BaseScreen(
    baseScreenViewModel: BaseScreenViewModel,
    mainScreenViewModel: MainScreenViewModel,
    chatsScreenViewModel: ChatsScreenViewModel,
    agentsScreenViewModel: AgentsScreenViewModel,
    knowledgeScreenViewModel: KnowledgeScreenViewModel,
    decreesScreenViewModel: DecreesScreenViewModel,
    skillsScreenViewModel: SkillsScreenViewModel,
    profileScreenViewModel: ProfileScreenViewModel,
    systemScreenViewModel: SystemScreenViewModel
) {
    val currentAlert = baseScreenViewModel.currentAlert.collectAsState()
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
    baseScreenViewModel.navToScreenCallback = { navToScreen(it) }

    LaunchedEffect(navBackStackEntry) {
        // Called if current nav changes
    }

    MainBase(
        curDestination = curDestination,
        currentAlert = currentAlert.value,
        dismissAlert = { baseScreenViewModel.dismissAlert() },
        navToScreen = { navToScreen(it) },
    ) { modifier ->
        AppNavHost(
            modifier = modifier,
            navController = navController,
            startDestination = Destination.MAIN,
            navToScreen = { navToScreen(it) },
            mainScreenViewModel = mainScreenViewModel,
            chatsScreenViewModel = chatsScreenViewModel,
            agentsScreenViewModel = agentsScreenViewModel,
            knowledgeScreenViewModel = knowledgeScreenViewModel,
            decreesScreenViewModel = decreesScreenViewModel,
            skillsScreenViewModel = skillsScreenViewModel,
            profileScreenViewModel = profileScreenViewModel,
            systemScreenViewModel = systemScreenViewModel
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
    currentAlert: AlertNotification? = null,
    dismissAlert: () -> Unit = {},
    navToScreen: (Destination) -> Unit = {},
    content: @Composable (Modifier) -> Unit = {}
) {
    val railContent by curDestination.railContent.collectAsState()
    val defaultNavWidth = 210
    var navWidth by remember { mutableStateOf(defaultNavWidth) }
    val scrollState = rememberScrollState()
    val scrollModifier = railContent?.let { Modifier } ?: Modifier.verticalScroll(scrollState)

    var isRailCollapsed by remember { mutableStateOf(false) }
    val visibleRailWidth by animateDpAsState(
        targetValue = if (isRailCollapsed) 0.dp else navWidth.dp,
        label = "visibleRailWidth"
    )
    val arrowRotation by animateFloatAsState(
        targetValue = if (isRailCollapsed) 180f else 0f,
        label = "collapseArrowRotation"
    )

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = visibleRailWidth)
            ) {
                content(Modifier.fillMaxSize())
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(navWidth.dp)
                    .offset(x = visibleRailWidth - navWidth.dp)
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
                        .height(815.dp)
                        .align(Alignment.TopStart)
                        .then(scrollModifier)
                ) {
                    railContent?.let { rc ->
                        val rContent = rc.content ?: return@let
                        navWidth = rc.width
                        rContent(Modifier)
                    } ?: run {
                        navWidth = defaultNavWidth
                        Image(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp)
                        .background(backgroundColor)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(vertical = 20.dp),
                        text = "v${BuildInfo.VERSION}\n(${BuildInfo.BUILD})",
                        color = textColor.copy(alpha = 0.55f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp
                    )
                }
            }
            RailCollapseButton(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .offset(x = (visibleRailWidth - 16.dp).coerceAtLeast(8.dp)),
                rotation = arrowRotation,
                isCollapsed = isRailCollapsed,
                onToggle = { isRailCollapsed = !isRailCollapsed }
            )
            AlertView(
                modifier = Modifier.fillMaxSize(),
                currentAlert = currentAlert,
                onDismiss = dismissAlert
            )
        }
    }
}

@Composable
private fun RailCollapseButton(
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    isCollapsed: Boolean = false,
    onToggle: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, surfaceColor, CircleShape)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation),
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = if (isCollapsed) "Expand navigation" else "Collapse navigation",
            tint = textColor
        )
    }
}

@Composable
fun HeaderScreen(
    modifier: Modifier = Modifier,
    title: String = "System",
    subtitle: String = "Manage your kingdom's infrastructure, connections, and system settings.",
    destination: Destination,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(destination.icon),
                    contentDescription = "",
                    alignment = Alignment.Center,
                    colorFilter = ColorFilter.tint(primaryColor),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 27.sp,
                    color = textColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal
                )
            }
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = subtitle,
                fontSize = 12.sp,
                color = textColor.copy(0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}