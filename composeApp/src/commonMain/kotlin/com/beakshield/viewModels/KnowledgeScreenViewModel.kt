package com.beakshield.viewModels

import com.beakshield.BeakShieldApp.dawson
import com.beakshield.memory.Memory.MAX_SEARCH_QUERY_CHARS
import com.beakshield.memory.Memory.MAX_SEARCH_RESULTS
import com.beakshield.tablecells.DomainCellViewModel
import com.beakshield.tablecells.KnowledgeCellViewModel
import com.beakshield.websocket.memory.MemoryData
import com.beakshield.websocket.memory.MemoryDeleteResult
import com.beakshield.websocket.memory.MemoryDrawer
import com.beakshield.websocket.memory.MemoryDrawerPage
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

    // DETAIL DRAWER HANDLING
    private val _detailDrawer = MutableStateFlow<MemoryDrawer?>(null)
    val detailDrawer = _detailDrawer.asStateFlow()

    private val _pendingEntryID = MutableStateFlow<String?>(null)
    private val _pendingDelete = MutableStateFlow<MemoryDrawer?>(null)
    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError = _deleteError.asStateFlow()

    val isLoadingFullDrawer: StateFlow<Boolean> = _pendingEntryID.map { it != null }
        .stateIn(scope, SharingStarted.Eagerly, false)

    val isDeletingDrawer: StateFlow<Boolean> = _pendingDelete.map { it != null }
        .stateIn(scope, SharingStarted.Eagerly, false)

    // REQUEST UUIDs
    private val _overviewRequestUUID = MutableStateFlow<String?>(null)
    private val _wingsRequestUUID = MutableStateFlow<String?>(null)
    private val _allKnowledgeRequestUUID = MutableStateFlow<String?>(null)
    private val _searchRequestUUID = MutableStateFlow<String?>(null)
    private val _entryRequestUUID = MutableStateFlow<String?>(null)
    private val _deleteRequestUUID = MutableStateFlow<String?>(null)

    private val _lastKnowledgeViewedAt = MutableStateFlow(0L)   // TODO: Persist between sessions
    private val _memoryOverview = MutableStateFlow<MemoryOverview?>(null)
    val memoryOverview = _memoryOverview.asStateFlow()
    private val _memoryWings = MutableStateFlow<MemoryWingList?>(null)

    // ALL KNOWLEDGE
    private val _showAllKnowledge = MutableStateFlow(false)
    val showAllKnowledge = _showAllKnowledge.asStateFlow()
    private val _allKnowledgePage = MutableStateFlow<MemoryDrawerPage?>(null)
    private val _isLoadingAllKnowledge = MutableStateFlow(false)
    val isLoadingAllKnowledge = _isLoadingAllKnowledge.asStateFlow()

    val allKnowledgeCellViewModels: StateFlow<List<KnowledgeCellViewModel>> =
        combine(_allKnowledgePage, _lastKnowledgeViewedAt) { page, lastViewedAt ->
            getKnowledgeCellViewModels(page?.drawers ?: emptyList(), lastViewedAt)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val allKnowledgeTotal: StateFlow<Int> = _allKnowledgePage
        .map { it?.total ?: 0 }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    val canLoadMoreKnowledge: StateFlow<Boolean> = _allKnowledgePage
        .map { page -> page != null && page.drawers.size < page.total }
        .stateIn(scope, SharingStarted.Eagerly, false)

    // DOMAINS
    private val _showAllDomains = MutableStateFlow(false)
    val showAllDomains = _showAllDomains.asStateFlow()
    private val _pendingSearchQuery = MutableStateFlow<String?>(null)

    val allDomainCellViewModels: StateFlow<List<DomainCellViewModel>> =
        _memoryWings.map { wings ->
            getDomainCellViewModels(wings ?: return@map emptyList())
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val domainCellViewModels: StateFlow<List<DomainCellViewModel>> =
        combine(_memoryWings, _memoryOverview) { wings, _ ->
            getDomainCellViewModels(wings ?: return@combine emptyList())
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // SEARCH RELATED
    private val _searchResults = MutableStateFlow<MemorySearchResults?>(null)
    val searchResults = _searchResults.asStateFlow()
    private val _searchLimit = MutableStateFlow(DEFAULT_NUM_BATCH_RESULTS)
    private val _lastSearchQuery = MutableStateFlow<String?>(null)
    val lastSearchQuery = _lastSearchQuery.asStateFlow()

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
                lastUpdated = cells.firstOrNull()?.drawer?.filedAtFormattedRelative ?: "---"
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
            combine(_allKnowledgeRequestUUID, dawson.memoryResponses) { requestUUID, responses ->
                requestUUID?.let { responses[it] }
            }.collect { response ->
                if (response?.dataType == MemoryData.DataType.PAGE_ENTRIES) {
                    response.payloadAs<MemoryDrawerPage>()?.let { page ->
                        _allKnowledgePage.value = if (page.offset == 0) {
                            page
                        } else {
                            page.copy(
                                drawers = (_allKnowledgePage.value?.drawers ?: emptyList()) + page.drawers,
                                offset = 0
                            )
                        }
                    }
                    _isLoadingAllKnowledge.value = false
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

        scope.launch {
            combine(_entryRequestUUID, dawson.memoryResponses) { requestUUID, responses ->
                requestUUID?.let { responses[it] }
            }.collect { response ->
                if (response?.dataType == MemoryData.DataType.ENTRY) {
                    response.payloadAs<MemoryDrawer>()?.let { full ->
                        // Only upgrade if the popup is still showing this drawer
                        if (_detailDrawer.value?.id == full.id) {
                            _detailDrawer.value = full
                        }
                    }
                    _pendingEntryID.value = null
                }
            }
        }

        scope.launch {
            combine(_deleteRequestUUID, dawson.memoryResponses) { requestUUID, responses ->
                requestUUID?.let { responses[it] }
            }.collect { response ->
                if (response?.dataType == MemoryData.DataType.DELETE) {
                    val result = response.payloadAs<MemoryDeleteResult>()
                    val deleted = _pendingDelete.value
                    _pendingDelete.value = null
                    if ((result?.success == true) && (deleted != null)) {
                        removeDrawerLocally(deleted)
                        closeDrawerDetail()
                        requestOverview()
                    } else {
                        _deleteError.value = result?.error
                            ?: "Delete failed — the memory may not have been found."
                    }
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

    private fun getKnowledgeCellViewModels(drawers: List<MemoryDrawer>, lastViewedAt: Long): List<KnowledgeCellViewModel> {
        return drawers.mapIndexed { index, drawer ->
            KnowledgeCellViewModel(
                id = index.toLong(),
                drawer = drawer,
                onSelect = {
                    showDrawerDetail(drawer)
                },
                onWingRoomClick = {
                    // TODO: Navigate to wing/room browse view
                }
            ).apply {
                isNew = ((drawer.filedAtTimestamp ?: 0L) > lastViewedAt)
            }
        }
            .sortedByDescending { it.drawer.filedAtTimestamp ?: 0L }
    }

    private fun getSearchResultCellViewModels(drawers: List<MemoryDrawer>): List<KnowledgeCellViewModel> {
        // NOTE: Search hits do not carry drawerId
        return drawers.mapIndexed { index, drawer ->
            KnowledgeCellViewModel(
                id = index.toLong(),
                drawer = drawer,
                onSelect = {
                    showDrawerDetail(drawer)
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

        _lastSearchQuery.value = trimmed
        _searchLimit.value = DEFAULT_NUM_BATCH_RESULTS  // new query resets the window
        fireSearch(trimmed, DEFAULT_NUM_BATCH_RESULTS)
    }

    fun loadMoreSearchResults() {
        val query = _lastSearchQuery.value ?: return
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
        _lastSearchQuery.value = null
        _pendingSearchQuery.value = null
        _searchRequestUUID.value = null
    }

    fun openAllKnowledge() {
        clearSearch()
        _showAllDomains.value = false
        _showAllKnowledge.value = true
        if (_allKnowledgePage.value == null) {
            requestAllKnowledge(DEFAULT_NUM_BATCH_RESULTS)
        }
    }

    fun loadMoreKnowledge() {
        val page = _allKnowledgePage.value ?: return
        if (_isLoadingAllKnowledge.value || page.drawers.size >= page.total) return
        requestAllKnowledge(limit = DEFAULT_NUM_BATCH_RESULTS, offset = page.drawers.size)
    }

    private fun requestAllKnowledge(limit: Int, offset: Int = 0) {
        val dataUUID = Uuid.random().toString()
        _allKnowledgeRequestUUID.value = dataUUID
        _isLoadingAllKnowledge.value = true
        dawson.requestMemory(
            dataUUID = dataUUID,
            dataType = MemoryData.DataType.PAGE_ENTRIES,
            query = MemoryQuery(limit = limit, offset = offset)
        )
    }

    fun closeAllKnowledge() {
        _showAllKnowledge.value = false
    }

    fun openAllDomains() {
        clearSearch()
        _showAllKnowledge.value = false
        _showAllDomains.value = true
    }

    fun closeAllDomains() {
        _showAllDomains.value = false
    }

    fun markKnowledgeViewed() {
        _lastKnowledgeViewedAt.value = Clock.System.now().toEpochMilliseconds()
    }

    fun showDrawerDetail(drawer: MemoryDrawer) {
        _deleteError.value = null
        _detailDrawer.value = drawer
        // Lists carry previews; fetch the full entry when we have an id.
        // Search hits have no id but already carry full content — no fetch.
        if (drawer.id.isNotEmpty()) {
            val dataUUID = Uuid.random().toString()
            _entryRequestUUID.value = dataUUID
            _pendingEntryID.value = drawer.id
            dawson.requestMemory(
                dataUUID = dataUUID,
                dataType = MemoryData.DataType.ENTRY,
                query = MemoryQuery(drawerID = drawer.id)
            )
        }
    }

    fun closeDrawerDetail() {
        _detailDrawer.value = null
        _pendingEntryID.value = null
        _deleteError.value = null
    }

    fun requestDeleteDrawer(drawer: MemoryDrawer) {
        if (_pendingDelete.value != null) return
        _deleteError.value = null
        _pendingDelete.value = drawer

        val dataUUID = Uuid.random().toString()
        _deleteRequestUUID.value = dataUUID
        dawson.requestMemory(
            dataUUID = dataUUID,
            dataType = MemoryData.DataType.DELETE,
            query = MemoryQuery(
                drawerID = drawer.id.takeIf { it.isNotEmpty() },
                // No id (search hit) → server resolves via duplicate check on verbatim content
                content = drawer.content.takeIf { drawer.id.isEmpty() }
            )
        )
    }

    private fun removeDrawerLocally(drawer: MemoryDrawer) {
        _searchResults.value = _searchResults.value?.let { results ->
            results.copy(results = results.results.filterNot {
                (it === drawer) || ((drawer.id.isNotEmpty()) && (it.id == drawer.id))
            })
        }
        _allKnowledgePage.value = _allKnowledgePage.value?.let { page ->
            page.copy(
                drawers = page.drawers.filterNot {
                    (it === drawer) || ((drawer.id.isNotEmpty()) && (it.id == drawer.id))
                },
                total = (page.total - 1).coerceAtLeast(0)
            )
        }
    }

    fun openWing(wing: String) {
        // TODO: Navigate to wing browse view (rooms within wing)
    }

    fun openRoom(wing: String, room: String) {
        // TODO: Navigate to room browse view (entries within room)
    }

    companion object {
        private const val MAX_DOMAIN_CARDS = 5
        private const val DEFAULT_NUM_BATCH_RESULTS = 15
    }
}