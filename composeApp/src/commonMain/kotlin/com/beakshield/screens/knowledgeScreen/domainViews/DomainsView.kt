package com.beakshield.screens.knowledgeScreen.domainViews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.dawsonGold
import com.beakshield.tablecells.DomainCellViewModel
import com.beakshield.textSecondaryColor

@Preview(device = TABLET)
@Composable
fun DomainsView(
    modifier: Modifier = Modifier,
    domainCellViewModels: List<DomainCellViewModel> = DomainCellViewModel.MockDomainCVM.mockDomainCVMs,
    onClose: () -> Unit = {}
) {
    val padBetween = 12

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = padBetween.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "All Domains",
                    fontFamily = FontFamily.Serif,
                    color = dawsonGold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "${domainCellViewModels.size} domain${if (domainCellViewModels.size == 1) "" else "s"} across your kingdom's knowledge.",
                    color = textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClose() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Back to Recents",
                    color = dawsonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                Icon(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp),
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = dawsonGold
                )
            }
        }
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            columns = GridCells.Adaptive(minSize = 170.dp),
            horizontalArrangement = Arrangement.spacedBy(padBetween.dp),
            verticalArrangement = Arrangement.spacedBy(padBetween.dp)
        ) {
            items(domainCellViewModels, key = { it.id }) { cellViewModel ->
                DomainCard(
                    cellViewModel = cellViewModel
                )
            }
        }
    }
}