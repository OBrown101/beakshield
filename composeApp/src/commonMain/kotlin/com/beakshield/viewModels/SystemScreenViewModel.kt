package com.beakshield.viewModels

import com.beakshield.BeakShieldApp.Companion.dawson
import com.beakshield.BeakShieldApp.Companion.preferences
import com.beakshield.dawson.Provider
import com.beakshield.dawson.Server
import com.beakshield.tablecells.ProviderCellViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SystemScreenViewModel : VModel {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _railContent = MutableStateFlow<RailContent?>(null)
    override val railContent = _railContent.asStateFlow()

    private val _providerTypeSelected = MutableStateFlow<Provider.ProviderType?>(null)
    val providerTypeSelected = _providerTypeSelected.asStateFlow()

    private val _currentServer = MutableStateFlow<Server?>(null)
    val currentServer = _currentServer.asStateFlow()

    val providerCellViewModels: StateFlow<List<ProviderCellViewModel>> =
        dawson.activeProviders.map { providers ->
            providers.sortedBy { it.type }.mapIndexed { idx, provider ->
                getProviderCellViewModel(idx, provider)
            }
        }
            .stateIn(scope, SharingStarted.Lazily, emptyList())

    init {
        _currentServer.value = Server(
            address = preferences.serverAddress,
            port = preferences.serverPort
        )
    }

    private fun getProviderCellViewModel(index: Int, provider: Provider): ProviderCellViewModel {
        return ProviderCellViewModel(
            id = index.toLong(),
            provider = provider,
            onSelect = {
                selectProvider(provider)
            }
        )
    }

    fun selectProvider(provider: Provider?) {
        _providerTypeSelected.value = provider?.type
    }

    fun updateAPIKey(apiKey: String) {
        val type = _providerTypeSelected.value ?: return
        dawson.updateProviderAPIKeys(mapOf(type to apiKey))
    }

    fun connectToServer(server: Server) {
        _currentServer.value = server
        preferences.serverAddress = server.address
        preferences.serverPort = server.port
        dawson.connect(server.address, server.port)
    }
}