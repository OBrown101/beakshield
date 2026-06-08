package com.beakshield.tablecells

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.beakshield.composables.TableCellViewModel
import com.beakshield.dawson.Provider

data class ProviderCellViewModel(
    override val id: Long,
    val provider: Provider,
    val onSelect: () -> Unit
) : TableCellViewModel {
    override var selected by mutableStateOf(false)
    override var swipeState by mutableStateOf(TableCellViewModel.SwipeAnchor.Start)

    object MockProviderCVM {
        val mockProviderCVMs = listOf(
            ProviderCellViewModel(
                id = 0L,
                provider = Provider.MockProvider.mockProviders[0],
                onSelect = {}
            ),
            ProviderCellViewModel(
                id = 1L,
                provider = Provider.MockProvider.mockProviders[1],
                onSelect = {}
            ),
            ProviderCellViewModel(
                id = 2L,
                provider = Provider.MockProvider.mockProviders[2],
                onSelect = {}
            )
        )
    }
}