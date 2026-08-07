package com.beakshield.viewModels

import com.beakshield.BeakShieldApp.dawson
import com.beakshield.memory.Memory.MAX_SEARCH_QUERY_CHARS
import com.beakshield.memory.Memory.MAX_SEARCH_RESULTS
import com.beakshield.tablecells.DomainCellViewModel
import com.beakshield.tablecells.KnowledgeCellViewModel
import com.beakshield.websocket.memory.MemoryData
import com.beakshield.websocket.memory.MemoryDrawer
import com.beakshield.websocket.memory.MemoryOverview
import com.beakshield.websocket.memory.MemoryQuery
import com.beakshield.websocket.memory.MemorySearchResults
import com.beakshield.websocket.memory.MemoryWingList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class KnowledgeScreenViewModel : VModel {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var memoryTimerJob: Job? = null

    private val _railContent = MutableStateFlow<RailContent?>(null)
    override val railContent = _railContent.asStateFlow()

    // REQUEST UUIDs
    private val _overviewRequestUUID = MutableStateFlow<String?>(null)
    private val _wingsRequestUUID = MutableStateFlow<String?>(null)
    private val _searchRequestUUID = MutableStateFlow<String?>(null)

    private val _lastKnowledgeViewedAt = MutableStateFlow(0L)   // TODO: Persist between sessions
    private val _memoryOverview = MutableStateFlow<MemoryOverview?>(null)
    val memoryOverview = _memoryOverview.asStateFlow()
    private val _memoryWings = MutableStateFlow<MemoryWingList?>(null)

    // DOMAINS
    private val _showAllDomains = MutableStateFlow(false)
    val showAllDomains = _showAllDomains.asStateFlow()
    private val _pendingSearchQuery = MutableStateFlow<String?>(null)

    val allDomainCellViewModels: StateFlow<List<DomainCellViewModel>> =
        _memoryWings.map { wings ->
            getDomainCellViewModels(wings ?: return@map emptyList(), limit = null)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val domainCellViewModels: StateFlow<List<DomainCellViewModel>> =
        combine(_memoryWings, _memoryOverview) { wings, _ ->
            getDomainCellViewModels(wings ?: return@combine emptyList())
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // SEARCH RELATED
    private val _searchResults = MutableStateFlow<MemorySearchResults?>(null)
    val searchResults = _searchResults.asStateFlow()
    private val _searchLimit = MutableStateFlow(DEFAULT_NUM_BATCH_RESULTS)
    private var lastSearchQuery: String? = null

    val canLoadMoreResults: StateFlow<Boolean> =
        combine(_searchResults, _searchLimit) { results, limit ->
            (results != null) && (results.results.size >= limit) && (limit < MAX_SEARCH_RESULTS)
        }.stateIn(scope, SharingStarted.Eagerly, false)

    val searchResultCellViewModels: StateFlow<List<KnowledgeCellViewModel>> =
        _searchResults.map { results ->
            getSearchResultCellViewModels(results?.results ?: emptyList())
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val isSearching: StateFlow<Boolean> = _pendingSearchQuery.map { it != null }
        .stateIn(scope, SharingStarted.Eagerly, false)

    // RECENT KNOWLEDGE AND STATS
    val knowledgeCellViewModels: StateFlow<List<KnowledgeCellViewModel>> =
        combine(_memoryOverview, _lastKnowledgeViewedAt) { overview, lastViewedAt ->
            getKnowledgeCellViewModels(overview?.recents ?: emptyList(), lastViewedAt)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val knowledgeStatistics: StateFlow<KnowledgeStatistics> =
        combine(_memoryOverview, knowledgeCellViewModels) { overview, cells ->
            KnowledgeStatistics(
                totalKnowledge = overview?.status?.totalDrawers,
                domains = overview?.status?.wings,
                lastUpdated = cells.firstOrNull()?.filedAtFormattedRelative ?: "---"
            )
        }.stateIn(scope, SharingStarted.Eagerly, KnowledgeStatistics())

    init {
        scope.launch {
            combine(_overviewRequestUUID, dawson.memoryResponses) { requestUUID, responses ->
                requestUUID?.let { responses[it] }
            }.collect { response ->
                if (response?.dataType == MemoryData.DataType.OVERVIEW) {
                    response.payloadAs<MemoryOverview>()?.let { overview ->
                        _memoryOverview.value = overview
                    }
                }
            }
        }

        scope.launch {
            combine(_wingsRequestUUID, dawson.memoryResponses) { requestUUID, responses ->
                requestUUID?.let { responses[it] }
            }.collect { response ->
                if (response?.dataType == MemoryData.DataType.LIST_WINGS) {
                    response.payloadAs<MemoryWingList>()?.let { wings ->
                        _memoryWings.value = wings
                    }
                }
            }
        }

        scope.launch {
            combine(_searchRequestUUID, dawson.memoryResponses) { requestUUID, responses ->
                requestUUID?.let { responses[it] }
            }.collect { response ->
                if (response?.dataType == MemoryData.DataType.SEARCH) {
                    val results = response.payloadAs<MemorySearchResults>()
                    if (results != null) {
                        _searchResults.value = results
                    }
                    _pendingSearchQuery.value = null
                }
            }
        }

        startMemoryTimer()
    }

    data class KnowledgeStatistics(
        val totalKnowledge: Int? = null,
        val domains: Int? = null,
        val lastUpdated: String = "---"
    )

    private fun startMemoryTimer() {
        if (memoryTimerJob != null) return
        memoryTimerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                requestOverview()
                requestWings()
                delay(10000)
            }
        }
    }

    private fun getDomainCellViewModels(wings: MemoryWingList, limit: Int? = MAX_DOMAIN_CARDS): List<DomainCellViewModel> {
        val selected = limit?.let { wings.wings.take(it) } ?: wings.wings
        return selected.mapIndexed { index, wing ->
            DomainCellViewModel(
                id = index.toLong(),
                wing = wing,
                onSelect = {
                    // TODO: Navigate to wing browse view (rooms within wing)
                }
            )
        }
    }

    private fun getKnowledgeCellViewModels(drawers: List<MemoryDrawer>, lastViewedAt: Long): List<KnowledgeCellViewModel> {
        return drawers.mapIndexed { index, drawer ->
            KnowledgeCellViewModel(
                id = index.toLong(),
                drawer = drawer,
                onSelect = {
                    // TODO: Navigate to drawer detail view (full content + delete);
                    //  detail requires an ENTRY request — list drawers carry previews only
                },
                onWingRoomClick = {
                    // TODO: Navigate to wing/room browse view
                }
            ).apply {
                isNew = ((filedAtMillis ?: 0L) > lastViewedAt)
            }
        }
            .sortedByDescending { it.filedAtMillis ?: 0L }
    }

    private fun getSearchResultCellViewModels(drawers: List<MemoryDrawer>): List<KnowledgeCellViewModel> {
        // NOTE: Search hits do not carry drawerId
        return drawers.mapIndexed { index, drawer ->
            KnowledgeCellViewModel(
                id = index.toLong(),
                drawer = drawer,
                onSelect = {
                    // TODO: Expand cell inline (full content is already present)
                },
                onWingRoomClick = {
                    // TODO: Navigate to wing/room browse view
                }
            )
        }
    }

    private fun getDomainCellViewModels(wings: MemoryWingList): List<DomainCellViewModel> {
        return wings.wings      // pre-sorted by count desc server-side
            .take(MAX_DOMAIN_CARDS)
            .mapIndexed { index, wing ->
                DomainCellViewModel(
                    id = index.toLong(),
                    wing = wing,
                    onSelect = {
                        // TODO: Navigate to wing browse view (rooms within wing)
                    }
                )
            }
    }

    fun requestOverview() {
        val dataUUID = Uuid.random().toString()
        _overviewRequestUUID.value = dataUUID
        dawson.requestMemory(
            dataUUID = dataUUID,
            dataType = MemoryData.DataType.OVERVIEW,
            query = MemoryQuery(limit = 30)
        )
    }

    fun requestWings() {
        val dataUUID = Uuid.random().toString()
        _wingsRequestUUID.value = dataUUID
        dawson.requestMemory(
            dataUUID = dataUUID,
            dataType = MemoryData.DataType.LIST_WINGS,
            query = MemoryQuery()
        )
    }

    fun requestSearch(query: String) {
        val trimmed = query.trim().take(MAX_SEARCH_QUERY_CHARS)
        if (trimmed.isEmpty()) return

        lastSearchQuery = trimmed
        _searchLimit.value = DEFAULT_NUM_BATCH_RESULTS  // new query resets the window
        fireSearch(trimmed, DEFAULT_NUM_BATCH_RESULTS)
    }

    fun loadMoreSearchResults() {
        val query = lastSearchQuery ?: return
        if (_pendingSearchQuery.value != null) return

        val newLimit = minOf((_searchLimit.value + DEFAULT_NUM_BATCH_RESULTS), MAX_SEARCH_RESULTS)
        _searchLimit.value = newLimit
        fireSearch(query, newLimit)
    }

    private fun fireSearch(query: String, limit: Int) {
        val dataUUID = Uuid.random().toString()
        _searchRequestUUID.value = dataUUID
        _pendingSearchQuery.value = query
        dawson.requestMemory(
            dataUUID = dataUUID,
            dataType = MemoryData.DataType.SEARCH,
            query = MemoryQuery(query = query, limit = limit)
        )
    }

    fun clearSearch() {
        _searchResults.value = null
        _searchLimit.value = DEFAULT_NUM_BATCH_RESULTS
        lastSearchQuery = null
        _pendingSearchQuery.value = null
        _searchRequestUUID.value = null
    }

    fun openAllDomains() {
        clearSearch()
        _showAllDomains.value = true
    }

    fun closeAllDomains() {
        _showAllDomains.value = false
    }

    fun markKnowledgeViewed() {
        _lastKnowledgeViewedAt.value = Clock.System.now().toEpochMilliseconds()
    }

    companion object {
        private const val MAX_DOMAIN_CARDS = 5
        private const val DEFAULT_NUM_BATCH_RESULTS = 15
    }
}