package com.beakshield.server

import com.beakshield.dawson.Server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocalDawsonInstallController {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    sealed interface InstallState {
        data object Idle : InstallState
        data class Working(val step: String) : InstallState
        data class Success(val server: Server) : InstallState
        data class Failed(val message: String) : InstallState
    }

    private val _state = MutableStateFlow<InstallState>(InstallState.Idle)
    val state = _state.asStateFlow()

    private val _isInstalled = MutableStateFlow(isLocalDawsonInstalled())
    val isInstalled = _isInstalled.asStateFlow()

    val isAvailable: Boolean
        get() = isLocalServerInstallSupported

    fun setUp() {
        if (_state.value is InstallState.Working) return

        scope.launch {
            val coreDir = locateDawsonCoreDir()
            if (coreDir == null) {
                _state.value = InstallState.Failed(
                    "Couldn't find the DAWSON folder. Move the DAWSON folder you downloaded into your home folder (so it sits at ~/DAWSON), then try again."
                )
                return@launch
            }

            _state.value = InstallState.Working("Installing the DAWSON server...")
            installLocalDawson(coreDir)?.let { error ->
                _state.value = InstallState.Failed(error)
                return@launch
            }

            _state.value = InstallState.Working("Starting DAWSON for the first time...")
            val credentials = awaitLocalDawsonCredentials(coreDir)
            if (credentials == null) {
                _state.value = InstallState.Failed(
                    "DAWSON started but didn't produce its credentials in time. Check ~/DAWSON/logs/dawson-error.log."
                )
                return@launch
            }

            _isInstalled.value = true
            _state.value = InstallState.Success(
                Server(
                    address = "127.0.0.1",
                    port = 8443,
                    authKey = credentials.authKey,
                    fingerprint = credentials.fingerprint
                )
            )
        }
    }

    fun reset() {
        _state.value = InstallState.Idle
    }

    fun stopLocalServer(onStopped: () -> Unit = {}) {
        if (_state.value is InstallState.Working) return
        scope.launch {
            _state.value = InstallState.Working("Stopping the DAWSON server...")
            val error = uninstallLocalDawson()
            _isInstalled.value = isLocalDawsonInstalled()
            if (error != null) {
                _state.value = InstallState.Failed(error)
            } else {
                _state.value = InstallState.Idle
                onStopped()
            }
        }
    }
}