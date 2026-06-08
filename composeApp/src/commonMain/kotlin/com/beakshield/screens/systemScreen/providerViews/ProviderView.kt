package com.beakshield.screens.systemScreen.providerViews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.beakshield.screens.systemScreen.SystemBox
import com.beakshield.tablecells.ProviderCellViewModel


@Preview
@Composable
fun ProvidersView(
    modifier: Modifier = Modifier,
    providerCellViewModels: List<ProviderCellViewModel> = ProviderCellViewModel.MockProviderCVM.mockProviderCVMs
) {
    SystemBox(
        modifier = modifier.fillMaxWidth(),
        title = "Providers",
        iconVector = Icons.Outlined.Cloud
    ) {
        ProviderTableView(
            modifier = Modifier,
            providerCellViewModels = providerCellViewModels
        )
    }
}