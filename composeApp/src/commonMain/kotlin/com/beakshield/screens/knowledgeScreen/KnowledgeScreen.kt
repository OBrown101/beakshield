package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beakshield.screens.Destination
import com.beakshield.screens.baseScreen.HeaderScreen
import com.beakshield.viewModels.KnowledgeScreenViewModel

@Preview(device = TABLET)
@Composable
fun KnowledgeScreen(
    modifier: Modifier = Modifier,
    knowledgeScreenViewModel: KnowledgeScreenViewModel = KnowledgeScreenViewModel(),
    navToScreen: (Destination) -> Unit = {}
) {
    val userInputFocusReq = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val knowledgeCellViewModels by knowledgeScreenViewModel.knowledgeCellViewModels.collectAsState()
    val domainCellViewModels by knowledgeScreenViewModel.domainCellViewModels.collectAsState()
    val knowledgeStatistics by knowledgeScreenViewModel.knowledgeStatistics.collectAsState()

    val padBetween = 12

    HeaderScreen(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        title = "Knowledge",
        subtitle = "Explore the knowledge your kingdom has acquired.",
        destination = Destination.KNOWLEDGE
    ) {
        Column() {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(padBetween.dp)
            ) {
                KnowledgeBannerView(
                    modifier = Modifier.weight(1f),
                    popularSearches = listOf("USBManager", "Kotlin Coroutines", "Compose Navigation", "Email Tone"), // TODO: Connect to saved string list
                    onSearch = {
                        knowledgeScreenViewModel.requestSearch(it)
                    },
                    onPopularSearchClick = {
                        knowledgeScreenViewModel.requestSearch(it)
                    }
                )
                StatisticsView(
                    modifier = Modifier.width(250.dp),
                    totalKnowledge = knowledgeStatistics.totalKnowledge ?: 0,
                    domains = knowledgeStatistics.domains ?: 0,
                    lastUpdated = knowledgeStatistics.lastUpdated
                )
            }
            RecentKnowledgeView(
                modifier = Modifier
                    .padding(top = padBetween.dp)
                    .weight(1f),
                knowledgeCellViewModels = knowledgeCellViewModels
            )
            KnowledgeInsightsView(
                modifier = Modifier.padding(top = padBetween.dp),
                domainCellViewModels = domainCellViewModels
//                onViewAllDomains =       // TODO: Navigate to wings browse view
            )
        }
    }
}
