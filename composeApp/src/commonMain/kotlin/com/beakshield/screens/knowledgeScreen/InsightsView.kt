package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.composables.BasicBox
import com.beakshield.dawsonGold
import com.beakshield.tablecells.DomainCellViewModel
import com.beakshield.textColor
import com.beakshield.textSecondaryColor

@Preview(device = TABLET)
@Composable
fun KnowledgeInsightsView(
    modifier: Modifier = Modifier,
    domainCellViewModels: List<DomainCellViewModel> = DomainCellViewModel.MockDomainCVM.mockDomainCVMs,
    onViewAllDomains: () -> Unit = {}
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
                    text = "Knowledge Insights",
                    fontFamily = FontFamily.Serif,
                    color = dawsonGold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "Areas where your kingdom has the most knowledge.",
                    color = textSecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onViewAllDomains() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All Domains",
                    color = dawsonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = dawsonGold
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(padBetween.dp)
        ) {
            domainCellViewModels.forEach { cellViewModel ->
                DomainCard(
                    modifier = Modifier.weight(1f),
                    cellViewModel = cellViewModel
                )
            }
        }
    }
}

@Composable
private fun DomainCard(
    modifier: Modifier = Modifier,
    cellViewModel: DomainCellViewModel
) {
    val wingStyle = cellViewModel.wingStyle

    BasicBox(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { cellViewModel.onSelect() }
                .padding(horizontal = 12.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(wingStyle.color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, wingStyle.color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = wingStyle.icon,
                    contentDescription = null,
                    tint = wingStyle.color
                )
            }
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = cellViewModel.displayName,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = cellViewModel.entryCount.toString(),
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "knowledge entries",
                color = textSecondaryColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
