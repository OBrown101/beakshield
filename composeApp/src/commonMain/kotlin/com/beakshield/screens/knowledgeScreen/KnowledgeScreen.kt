package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(padBetween.dp)
        ) {
            KnowledgeBannerView(
                modifier = Modifier.weight(1f),
//                popularSearches = ,
//                onSearch = ,
//                onPopularSearchClick =
            )
            StatisticsView(
                modifier = Modifier.width(250.dp)
//                totalKnowledge = ,
//                domains = ,
//                lastUpdated = ,
//                storageUsed = ,
//                onViewFullStats =
            )
        }
    }
}