package com.beakshield.viewModels

import com.beakshield.BeakShieldApp.dawson
import com.beakshield.memory.Memory.MAX_SEARCH_QUERY_CHARS
import com.beakshield.memory.Memory.MAX_SEARCH_RESULTS
import com.beakshield.tablecells.DomainCellViewModel
import com.beakshield.tablecells.KnowledgeCellViewModel
import com.beakshield.tablecells.TopicCellViewModel
import com.beakshield.websocket.memory.MemoryData
import com.beakshield.websocket.memory.MemoryDeleteResult
import com.beakshield.websocket.memory.MemoryDrawer
import com.beakshield.websocket.memory.MemoryDrawerPage
import com.beakshield.websocket.memory.MemoryOverview
import com.beakshield.websocket.memory.MemoryQuery
import com.beakshield.websocket.memory.MemoryRoomList
import com.beakshield.websocket.memory.MemorySearchResults
import com.beakshield.websocket.memory.MemoryWingList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
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
    private val _roomsRequestUUID = MutableStateFlow<String?>(null)
    private val _pageRequestUUID = MutableStateFlow<String?>(null)
    private val _allKnowledgeRequestUUID = MutableStateFlow<String?>(null)
    private val _searchRequestUUID = MutableStateFlow<String?>(null)
    private val _entryRequestUUID = MutableStateFlow<String?>(null)
    private val _deleteRequestUUID = MutableStateFlow<String?>(null)

    private val _lastKnowledgeViewedAt = MutableStateFlow(0L)   // TODO: Persist between sessions
    private val _memoryOverview = MutableStateFlow<MemoryOverview?>(null)
    val memoryOverview = _memoryOverview.asStateFlow()
    private val _memoryWings = MutableStateFlow<MemoryWingList?>(null)

    // BROWSING WINGS/ROOMS ENTRIES
    private val _selectedBrowseWing = MutableStateFlow<String?>(null)
    private val _selectedBrowseRoom = MutableStateFlow<String?>(null)
    val selectedBrowseWing = _selectedBrowseWing.asStateFlow()
    val selectedBrowseRoom = _selectedBrowseRoom.asStateFlow()

    private val _memoryRooms = MutableStateFlow<MemoryRoomList?>(null)
    private val _browseEntries = MutableStateFlow<List<MemoryDrawer>>(emptyList())
    private val _browseTotal = MutableStateFlow(0)
    private val _pendingPage = MutableStateFlow(false)
    val browseTotal = _browseTotal.asStateFlow()

    val isLoadingBrowsePage: StateFlow<Boolean> = _pendingPage.stateIn(scope, SharingStarted.Eagerly, false)

    val canLoadMoreBrowse: StateFlow<Boolean> =
        combine(_browseEntries, _browseTotal) { entries, total ->
            entries.isNotEmpty() && (entries.size < total)
        }.stateIn(scope, SharingStarted.Eagerly, false)

    val topicCellViewModels: StateFlow<List<TopicCellViewModel>> =
        combine(_memoryRooms, _selectedBrowseWing) { rooms, wing ->
            val rooms = rooms ?: return@combine emptyList()
            val wing = wing ?: return@combine emptyList()
            getTopicCellViewModels(rooms, wing)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val browseKnowledgeCellViewModels: StateFlow<List<KnowledgeCellViewModel>> = _browseEntries.map { drawers ->
        getBrowseKnowledgeCellViewModels(drawers)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // ALL KNOWLEDGE
    private val _showAllKnowledge = MutableStateFlow(false)
    val showAllKnowledge = _showAllKnowledge.asStateFlow()
    private val _allKnowledgePage = MutableStateFlow<MemoryDrawerPage?>(null)
    private val _isLoadingAllKnowledge = MutableStateFlow(false)
    val isLoadingAllKnowledge = _isLoadingAllKnowledge.asStateFlow()

    val allKnowledgeCellViewModels: StateFlow<List<KnowledgeCellViewModel>> =
        combine(_allKnowledgePage, _lastKnowledgeViewedAt) { page, lastViewedAt ->
            val drawers = page?.drawers ?: emptyList()
            getKnowledgeCellViewModels(drawers, lastViewedAt)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val allKnowledgeTotal: StateFlow<Int> = _allKnowledgePage.map { it?.total ?: 0 }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    val canLoadMoreKnowledge: StateFlow<Boolean> = _allKnowledgePage.map { page ->
        (page != null) && (page.drawers.size < page.total)
    }.stateIn(scope, SharingStarted.Eagerly, false)

    // DOMAINS
    private val _showAllDomains = MutableStateFlow(false)
    val showAllDomains = _showAllDomains.asStateFlow()
    private val _pendingSearchQuery = MutableStateFlow<String?>(null)

    val allDomainCellViewModels: StateFlow<List<DomainCellViewModel>> = _memoryWings.map { wings ->
        val wings = wings ?: return@map emptyList()
        getDomainCellViewModels(wings)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val domainCellViewModels: StateFlow<List<DomainCellViewModel>> =
        combine(_memoryWings, _memoryOverview) { wings, _ ->
            val wings = wings ?: return@combine emptyList()
            getDomainCellViewModels(wings, limit = MAX_DOMAIN_CARDS)
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
            dawson.memoryResponses.collect { responses ->
                _overviewRequestUUID.value?.let { responses[it] }?.let {
                    handleOverviewData(it)  // Overview stats
                }
                _wingsRequestUUID.value?.let { responses[it] }?.let {
                    handleListWingsData(it) // All wings
                }
                _roomsRequestUUID.value?.let { responses[it] }?.let {
                    handleListRoomsData(it) // All rooms
                }
                _allKnowledgeRequestUUID.value?.let { responses[it] }?.let {
                    handleAllKnowledgeData(it) // All drawers (paginated)
                }
                _searchRequestUUID.value?.let { responses[it] }?.let {
                    handleSearchData(it)    // Search results
                }
                _entryRequestUUID.value?.let { responses[it] }?.let {
                    handleEntryData(it)     // Specific drawer entry
                }
                _deleteRequestUUID.value?.let { responses[it] }?.let {
                    handleDeleteData(it)    // Drawer delete
                }
                _pageRequestUUID.value?.let { responses[it] }?.let {
                    handlePageData(it)      // Specific paginated drawers request
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

    private fun handleOverviewData(data: MemoryData) {
        if (data.dataType != MemoryData.DataType.OVERVIEW) return
        data.payloadAs<MemoryOverview>()?.let {
            _memoryOverview.value = it
        }
    }

    private fun handleListWingsData(data: MemoryData) {
        if (data.dataType != MemoryData.DataType.LIST_WINGS) return
        data.payloadAs<MemoryWingList>()?.let {
            _memoryWings.value = it
        }
    }

    private fun handleListRoomsData(data: MemoryData) {
        if (data.dataType != MemoryData.DataType.LIST_ROOMS) return
        data.payloadAs<MemoryRoomList>()?.let {
            _memoryRooms.value = it
        }
    }

    private fun handleAllKnowledgeData(data: MemoryData) {
        if (data.dataType != MemoryData.DataType.PAGE_ENTRIES) return
        data.payloadAs<MemoryDrawerPage>()?.let { page ->
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

    private fun handleSearchData(data: MemoryData) {
        if (data.dataType != MemoryData.DataType.SEARCH) return
        data.payloadAs<MemorySearchResults>()?.let {
            _searchResults.value = it
        }
        _pendingSearchQuery.value = null
    }

    private fun handleEntryData(data: MemoryData) {
        if (data.dataType != MemoryData.DataType.ENTRY) return
        data.payloadAs<MemoryDrawer>()?.let { full ->
            if (_detailDrawer.value?.id == full.id) {
                // Only update to full if the drawer popup still open
                _detailDrawer.value = full
            }
        }
        _pendingEntryID.value = null
    }

    private fun handleDeleteData(data: MemoryData) {
        if (data.dataType != MemoryData.DataType.DELETE) return
        val result = data.payloadAs<MemoryDeleteResult>()
        val deleted = _pendingDelete.value
        _pendingDelete.value = null
        if ((result?.success == true) && (deleted != null)) {
            removeDrawerLocally(deleted)
            closeDrawerDetail()
            requestOverview()
        } else {
            _deleteError.value = result?.error ?: "Delete failed — the memory may not have been found."
        }
    }

    private fun handlePageData(data: MemoryData) {
        if (data.dataType != MemoryData.DataType.PAGE_ENTRIES) return
        data.payloadAs<MemoryDrawerPage>()?.let { page ->
            _browseTotal.value = page.total
            _browseEntries.value = if (page.offset == 0) {
                page.drawers
            } else {
                _browseEntries.value + page.drawers   // append next page
            }
        }
        _pendingPage.value = false
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
                    openRoom(drawer.wing, drawer.room)
                    // TODO: Eventually navigate to specific result entry in wing -> room -> drawer
                }
            ).apply {
                isNew = ((drawer.filedAtTimestamp ?: 0L) > lastViewedAt)
            }
        }.sortedByDescending { it.drawer.filedAtTimestamp ?: 0L }
    }

    private fun getSearchResultCellViewModels(drawers: List<MemoryDrawer>): List<KnowledgeCellViewModel> {
        return drawers.mapIndexed { index, drawer ->
            KnowledgeCellViewModel(
                id = index.toLong(),
                drawer = drawer,
                onSelect = {
                    showDrawerDetail(drawer)
                },
                onWingRoomClick = {
                    openRoom(drawer.wing, drawer.room)
                    // TODO: Eventually navigate to specific result entry in wing -> room -> drawer
                }
            )
        }
    }

    private fun getDomainCellViewModels(wings: MemoryWingList, limit: Int = 0): List<DomainCellViewModel> {
        return wings.wings
            .take(limit)
            .mapIndexed { index, wing ->
                DomainCellViewModel(
                    id = index.toLong(),
                    wing = wing,
                    onSelect = {
                        openWing(wing.name)
                    }
                )
            }
    }

    private fun getTopicCellViewModels(rooms: MemoryRoomList, wing: String): List<TopicCellViewModel> {
        return rooms.rooms.mapIndexed { index, room ->
            TopicCellViewModel(
                id = index.toLong(),
                wing = wing,
                room = room,
                onSelect = {
                    openRoom(wing, room.name)
                }
            )
        }
    }

    private fun getBrowseKnowledgeCellViewModels(drawers: List<MemoryDrawer>): List<KnowledgeCellViewModel> {
        return drawers.mapIndexed { index, drawer ->
            KnowledgeCellViewModel(
                id = index.toLong(),
                drawer = drawer,
                onSelect = {
                    showDrawerDetail(drawer)
                },
                onWingRoomClick = { /* Already inside this wing/room — no-op */ }
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

    fun requestRoomsForWing(wing: String) {
        val dataUUID = Uuid.random().toString()
        _roomsRequestUUID.value = dataUUID
        dawson.requestMemory(
            dataUUID = dataUUID,
            dataType = MemoryData.DataType.LIST_ROOMS,
            query = MemoryQuery(wing = wing)
        )
    }

    fun requestSearch(query: String) {
        val trimmed = query.trim().take(MAX_SEARCH_QUERY_CHARS)
        if (trimmed.isEmpty()) return

        _lastSearchQuery.value = trimmed
        _searchLimit.value = DEFAULT_NUM_BATCH_RESULTS  // new query resets the window
        sendSearchRequest(trimmed, DEFAULT_NUM_BATCH_RESULTS)
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

    private fun requestBrowsePage(offset: Int) {
        val wing = _selectedBrowseWing.value ?: return
        val room = _selectedBrowseRoom.value ?: return
        _pendingPage.value = true

        val dataUUID = Uuid.random().toString()
        _pageRequestUUID.value = dataUUID
        dawson.requestMemory(
            dataUUID = dataUUID,
            dataType = MemoryData.DataType.PAGE_ENTRIES,
            query = MemoryQuery(wing = wing, room = room, limit = BROWSE_PAGE_SIZE, offset = offset)
        )
    }

    private fun sendSearchRequest(query: String, limit: Int) {
        val dataUUID = Uuid.random().toString()
        _searchRequestUUID.value = dataUUID
        _pendingSearchQuery.value = query
        dawson.requestMemory(
            dataUUID = dataUUID,
            dataType = MemoryData.DataType.SEARCH,
            query = MemoryQuery(query = query, limit = limit)
        )
    }

    fun loadMoreSearchResults() {
        val query = _lastSearchQuery.value ?: return
        if (_pendingSearchQuery.value != null) return

        val newLimit = minOf((_searchLimit.value + DEFAULT_NUM_BATCH_RESULTS), MAX_SEARCH_RESULTS)
        _searchLimit.value = newLimit
        sendSearchRequest(query, newLimit)
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
        if (_isLoadingAllKnowledge.value || (page.drawers.size >= page.total)) return
        requestAllKnowledge(limit = DEFAULT_NUM_BATCH_RESULTS, offset = page.drawers.size)
    }

    fun closeAllKnowledge() {
        _showAllKnowledge.value = false
    }

    fun openAllDomains() {
        clearSearch()
        _showAllKnowledge.value = false
        _showAllDomains.value = true
        _selectedBrowseWing.value = null
        _selectedBrowseRoom.value = null
    }

    fun openWing(wing: String) {
        closeDrawerDetail()
        clearSearch()
        _showAllDomains.value = false
        _selectedBrowseRoom.value = null
        _browseEntries.value = emptyList()
        _memoryRooms.value = null
        _selectedBrowseWing.value = wing

        requestRoomsForWing(wing)
    }

    fun openRoom(wing: String, room: String) {
        closeDrawerDetail()
        clearSearch()
        _showAllDomains.value = false
        _selectedBrowseWing.value = wing
        _selectedBrowseRoom.value = room
        _browseEntries.value = emptyList()
        _browseTotal.value = 0
        requestBrowsePage(offset = 0)
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

    private fun removeDrawerLocally(drawer: MemoryDrawer) {
        _searchResults.value = _searchResults.value?.let { results ->
            val filteredResults = results.results.filterNot {
                (it === drawer) || ((drawer.id.isNotEmpty()) && (it.id == drawer.id))
            }
            results.copy(results = filteredResults)
        }
        _allKnowledgePage.value = _allKnowledgePage.value?.let { page ->
            val filteredDrawers = page.drawers.filterNot {
                (it === drawer) || ((drawer.id.isNotEmpty()) && (it.id == drawer.id))
            }
            page.copy(drawers = filteredDrawers, total = (page.total - 1).coerceAtLeast(0))
        }
    }

    fun loadMoreBrowseEntries() {
        if (_pendingPage.value) return
        requestBrowsePage(offset = _browseEntries.value.size)
    }

    fun closeTopicKnowledge() {
        _selectedBrowseRoom.value = null
        _browseEntries.value = emptyList()
    }

    fun closeTopics() {
        _selectedBrowseWing.value = null
        _memoryRooms.value = null
        _showAllDomains.value = true    // back lands on All Domains
    }

    companion object {
        private const val MAX_DOMAIN_CARDS = 5
        private const val DEFAULT_NUM_BATCH_RESULTS = 15
        private const val BROWSE_PAGE_SIZE = 25
    }
}