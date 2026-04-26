package com.beakshield

import androidx.lifecycle.viewmodel.compose.viewModel
import com.beakshield.dawson.Dawson
import com.beakshield.viewModels.BaseScreenViewModel
import com.beakshield.viewModels.MainScreenViewModel

class BeakShieldApp {

    companion object {
        val dawson by lazy {
            Dawson()
        }

        val baseScreenViewModel by lazy {
            BaseScreenViewModel()
        }

        val mainScreenViewModel by lazy {
            MainScreenViewModel()
        }
    }
}