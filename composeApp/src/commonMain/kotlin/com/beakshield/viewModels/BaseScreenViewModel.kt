package com.beakshield.viewModels

import com.beakshield.screens.Destination

class BaseScreenViewModel {

    var navToScreenCallback: (Destination) -> Unit = {}

    init {
    }

    fun navToScreen(destination: Destination) {
        navToScreenCallback(destination)
    }
}