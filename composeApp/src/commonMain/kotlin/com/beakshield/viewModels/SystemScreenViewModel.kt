package com.beakshield.viewModels

import com.beakshield.BeakShieldApp.dawson
import com.beakshield.BeakShieldApp.preferences
import com.beakshield.BeakShieldApp.voice
import com.beakshield.dawson.Provider
import com.beakshield.dawson.Server
import com.beakshield.server.LocalDawsonInstallController
import com.beakshield.tablecells.ProviderCellViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    val localInstallController = LocalDawsonInstallController()
    private val _showAutoSetupView = MutableStateFlow(true)
    val showAutoSetupView = _showAutoSetupView.asStateFlow()

    init {
        _currentServer.value = Server(
            address = preferences.serverAddress,
            port = preferences.serverPort,
            authKey = preferences.serverAuthToken,
            fingerprint = preferences.serverFingerprint
        )

        scope.launch {
            localInstallController.state.collect { state ->
                if (state is LocalDawsonInstallController.InstallState.Success) {
                    localInstallController.reset()
                    saveConnectToServer(state.server)
                    showAutoSetupView(false)
                }
            }
        }
    }

    fun showAutoSetupView(show: Boolean = true) {
        _showAutoSetupView.value = show
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

    fun updateProvider(apiKey: String, useOAuth: Boolean, preferredModelIDs: List<String>, defaultModelID: String) {
        val type = _providerTypeSelected.value ?: return
        val updatedProvider = dawson.activeProviders.value.firstOrNull { it.type == type }?.copy(
            apiKey = apiKey,
            useOAuth = useOAuth,
            preferredModelIDs = preferredModelIDs,
            defaultModelID = defaultModelID
        ) ?: return
        dawson.updateProvider(updatedProvider)
    }

    fun saveConnectToServer(server: Server) {
        _currentServer.value = server
        preferences.serverAddress = server.address
        preferences.serverPort = server.port
        preferences.serverAuthToken = server.authKey
        preferences.serverFingerprint = server.fingerprint
        dawson.connect()
    }

    fun clearDisconnectServer() {
        _currentServer.value = null
        preferences.serverAddress = ""
        preferences.serverPort = 0
        preferences.serverAuthToken = ""
        preferences.serverFingerprint = ""
        dawson.disconnect()
    }

    fun killLocalServer() {
        localInstallController.stopLocalServer(onStopped = {
            clearDisconnectServer()
        })
    }

    fun enableVoice() {
        voice.startModelDownload()
    }
}