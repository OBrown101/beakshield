package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beakshield.composables.beakshieldScrollbar
import com.beakshield.screens.Destination
import com.beakshield.screens.baseScreen.HeaderScreen
import com.beakshield.screens.knowledgeScreen.domainViews.DomainsOverviewView
import com.beakshield.screens.knowledgeScreen.domainViews.DomainsView
import com.beakshield.screens.knowledgeScreen.searchViews.SearchBannerView
import com.beakshield.screens.knowledgeScreen.searchViews.SearchResultsView
import com.beakshield.screens.knowledgeScreen.topicViews.TopicKnowledgeView
import com.beakshield.screens.knowledgeScreen.topicViews.TopicsView
import com.beakshield.viewModels.KnowledgeScreenViewModel
import kotlinx.coroutines.delay

@Preview(device = TABLET)
@Composable
fun ScreenBanner(
    modifier: Modifier = Modifier,
    knowledgeScreenViewModel: KnowledgeScreenViewModel = KnowledgeScreenViewModel(),
    navToScreen: (Destination) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val userInputFocusReq = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val knowledgeCellViewModels by knowledgeScreenViewModel.knowledgeCellViewModels.collectAsState()
    val knowledgeStatistics by knowledgeScreenViewModel.knowledgeStatistics.collectAsState()
    val showAllKnowledge by knowledgeScreenViewModel.showAllKnowledge.collectAsState()
    val allKnowledgeCellViewModels by knowledgeScreenViewModel.allKnowledgeCellViewModels.collectAsState()
    val allKnowledgeTotal by knowledgeScreenViewModel.allKnowledgeTotal.collectAsState()
    val canLoadMoreKnowledge by knowledgeScreenViewModel.canLoadMoreKnowledge.collectAsState()
    val isLoadingAllKnowledge by knowledgeScreenViewModel.isLoadingAllKnowledge.collectAsState()

    // TODO: I will either connect to saved list or just hardcode to common list
    val popularSearches = listOf("USBManager", "Kotlin Coroutines", "Compose Navigation", "Email Tone")

    val searchResults by knowledgeScreenViewModel.searchResults.collectAsState()
    val canLoadMoreResults by knowledgeScreenViewModel.canLoadMoreResults.collectAsState()
    val isSearching by knowledgeScreenViewModel.isSearching.collectAsState()
    val searchResultCellViewModels by knowledgeScreenViewModel.searchResultCellViewModels.collectAsState()
    val lastSearchQuery by knowledgeScreenViewModel.lastSearchQuery.collectAsState()

    val showAllDomains by knowledgeScreenViewModel.showAllDomains.collectAsState()
    val allDomainCellViewModels by knowledgeScreenViewModel.allDomainCellViewModels.collectAsState()
    val domainCellViewModels by knowledgeScreenViewModel.domainCellViewModels.collectAsState()

    val detailDrawer by knowledgeScreenViewModel.detailDrawer.collectAsState()
    val isLoadingFullDrawer by knowledgeScreenViewModel.isLoadingFullDrawer.collectAsState()
    val isDeletingDrawer by knowledgeScreenViewModel.isDeletingDrawer.collectAsState()
    val deleteError by knowledgeScreenViewModel.deleteError.collectAsState()

    val selectedBrowseWing by knowledgeScreenViewModel.selectedBrowseWing.collectAsState()
    val selectedBrowseRoom by knowledgeScreenViewModel.selectedBrowseRoom.collectAsState()
    val browseTotal by knowledgeScreenViewModel.browseTotal.collectAsState()
    val browseKnowledgeCellViewModels by knowledgeScreenViewModel.browseKnowledgeCellViewModels.collectAsState()
    val topicCellViewModels by knowledgeScreenViewModel.topicCellViewModels.collectAsState()
    val canLoadMoreBrowse by knowledgeScreenViewModel.canLoadMoreBrowse.collectAsState()
    val isLoadingBrowsePage by knowledgeScreenViewModel.isLoadingBrowsePage.collectAsState()

    val padBetween = 12

    LaunchedEffect(Unit) {
        delay(3000)
        knowledgeScreenViewModel.markKnowledgeViewed()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if ((searchResults != null) || showAllKnowledge || (selectedBrowseWing != null)) {
                            Modifier
                        } else {
                            Modifier
                                .verticalScroll(scrollState)
                                .beakshieldScrollbar(scrollState)
                        }
                    )
            ) {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(padBetween.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        SearchBannerView(
                            modifier = Modifier.height(310.dp),
                            query = lastSearchQuery,
                            popularSearches = popularSearches,
                            isSearching = isSearching,
                            searchActive = (searchResults != null),
                            onSearch = {
                                knowledgeScreenViewModel.requestSearch(it)
                            },
                            onClearSearch = {
                                knowledgeScreenViewModel.clearSearch()
                            },
                            onPopularSearchClick = {
                                knowledgeScreenViewModel.requestSearch(it)
                            }
                        )
                        when {
                            (searchResults != null) -> {
                                SearchResultsView(
                                    modifier = Modifier
                                        .padding(top = padBetween.dp)
                                        .weight(1f),
                                    query = searchResults?.query ?: "",
                                    searchResultCellViewModels = searchResultCellViewModels,
                                    canLoadMore = canLoadMoreResults,
                                    isLoadingMore = isSearching,
                                    onLoadMore = { knowledgeScreenViewModel.loadMoreSearchResults() },
                                    onClearSearch = { knowledgeScreenViewModel.clearSearch() }
                                )
                            }
                            showAllKnowledge -> {
                                AllKnowledgeView(
                                    modifier = Modifier
                                        .padding(top = padBetween.dp),
                                    knowledgeCellViewModels = allKnowledgeCellViewModels,
                                    totalKnowledge = allKnowledgeTotal,
                                    canLoadMore = canLoadMoreKnowledge,
                                    isLoadingMore = isLoadingAllKnowledge,
                                    onLoadMore = { knowledgeScreenViewModel.loadMoreKnowledge() },
                                    onClose = { knowledgeScreenViewModel.closeAllKnowledge() }
                                )
                            }
                            (selectedBrowseRoom != null) -> {
                                TopicKnowledgeView(
                                    modifier = Modifier.padding(top = padBetween.dp),
                                    wing = selectedBrowseWing ?: "",
                                    room = selectedBrowseRoom ?: "",
                                    totalEntries = browseTotal,
                                    knowledgeCellViewModels = browseKnowledgeCellViewModels,
                                    canLoadMore = canLoadMoreBrowse,
                                    isLoadingMore = isLoadingBrowsePage,
                                    onLoadMore = { knowledgeScreenViewModel.loadMoreBrowseEntries() },
                                    onBack = { knowledgeScreenViewModel.closeTopicKnowledge() }
                                )
                            }
                            (selectedBrowseWing != null) -> {
                                TopicsView(
                                    modifier = Modifier.padding(top = padBetween.dp),
                                    wing = selectedBrowseWing ?: "",
                                    topicCellViewModels = topicCellViewModels,
                                    onBack = { knowledgeScreenViewModel.closeTopics() }
                                )
                            }
                            showAllDomains -> {
                                DomainsView(
                                    modifier = Modifier.padding(top = padBetween.dp),
                                    domainCellViewModels = allDomainCellViewModels,
                                    onClose = { knowledgeScreenViewModel.closeAllDomains() }
                                )
                            }
                            else -> {
                                RecentKnowledgeView(
                                    modifier = Modifier
                                        .padding(top = padBetween.dp)
                                        .then(if (knowledgeCellViewModels.isEmpty()) Modifier else Modifier.height(420.dp)),
                                    knowledgeCellViewModels = knowledgeCellViewModels,
                                    onViewAll = { knowledgeScreenViewModel.openAllKnowledge() }
                                )
                                if (!showAllDomains) {
                                    DomainsOverviewView(
                                        modifier = Modifier.padding(top = padBetween.dp),
                                        domainCellViewModels = domainCellViewModels,
                                        onViewAllDomains = { knowledgeScreenViewModel.openAllDomains() }
                                    )
                                }
                            }
                        }
                    }
                    StatisticsView(
                        modifier = Modifier.width(250.dp),
                        statistics = knowledgeStatistics
                    )
                }
            }
        }
        detailDrawer?.let { drawer ->
            DrawerDetailView(
                drawer = drawer,
                isLoadingFull = isLoadingFullDrawer,
                isDeleting = isDeletingDrawer,
                deleteError = deleteError,
                onWingClick = { knowledgeScreenViewModel.openWing(it) },
                onRoomClick = { wing, room ->
                    knowledgeScreenViewModel.openRoom(wing, room)
                },
                onDelete = { knowledgeScreenViewModel.requestDeleteDrawer(it) },
                onDismiss = { knowledgeScreenViewModel.closeDrawerDetail() }
            )
        }
    }
}