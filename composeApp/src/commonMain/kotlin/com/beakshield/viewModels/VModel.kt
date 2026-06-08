package com.beakshield.viewModels

import kotlinx.coroutines.flow.StateFlow

interface VModel {
    val railContent: StateFlow<RailContent?>
}