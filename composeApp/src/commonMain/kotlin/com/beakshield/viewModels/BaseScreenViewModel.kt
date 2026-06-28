package com.beakshield.viewModels

import com.beakshield.BeakShieldApp.notifications
import com.beakshield.screens.Destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.legionarius.vector.notifications.AlertNotification

class BaseScreenViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)

    var navToScreenCallback: (Destination) -> Unit = {}

    private val alertQueue = ArrayDeque<AlertNotification>()
    private val _currentAlert = MutableStateFlow<AlertNotification?>(null)
    val currentAlert: StateFlow<AlertNotification?> = _currentAlert.asStateFlow()

    init {
        scope.launch {
            notifications.alerts.collect { newAlert ->
                alertQueue.addLast(newAlert)

                if (_currentAlert.value == null) {
                    showNextAlert()
                }
            }
        }
    }

    fun navToScreen(destination: Destination) {
        navToScreenCallback(destination)
    }

    private fun showNextAlert() {
        _currentAlert.value = alertQueue.removeFirstOrNull()
    }

    fun dismissAlert() {
        _currentAlert.value = null
        showNextAlert()
    }
}