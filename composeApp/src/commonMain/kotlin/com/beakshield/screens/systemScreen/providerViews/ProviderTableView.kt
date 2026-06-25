package com.beakshield.screens.systemScreen.providerViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.borderColor
import com.beakshield.composables.BasicBox
import com.beakshield.composables.TableView
import com.beakshield.dangerColor
import com.beakshield.darkGreenColor
import com.beakshield.dawsonGold
import com.beakshield.dawsonRed
import com.beakshield.lightGreenColor
import com.beakshield.surfaceColor
import com.beakshield.tablecells.ProviderCellViewModel
import com.beakshield.textPrimaryColor
import com.beakshield.textSecondaryColor

@Preview
@Composable
fun ProviderTableView(
    modifier: Modifier = Modifier,
    providerCellViewModels: List<ProviderCellViewModel> = ProviderCellViewModel.MockProviderCVM.mockProviderCVMs
) {
    TableView(
        modifier = modifier,
        cellViewModels = providerCellViewModels,
        cellHeight = { 55.dp },
        borderColor = Color.Transparent,
        cellSpacing = 3,
        cellOnClick = { cellViewModel ->
            providerCellViewModels.forEach { if (it.id != cellViewModel.id) it.selected = false }
            cellViewModel.selected = true
            cellViewModel.onSelect()
        },
    ) { modifier, cellViewModel ->
        ProviderTableCell(modifier, cellViewModel)
    }
}

@Composable
fun ProviderTableCell(
    modifier: Modifier = Modifier,
    cellViewModel: ProviderCellViewModel
) {
    val provider = cellViewModel.provider
    val needsConfig by remember(provider) {
        derivedStateOf {
            (provider.apiKey.isEmpty() && provider.models.isEmpty())
        }
    }

    BasicBox(
        modifier = modifier,
        bgColor = Color.Transparent,
        borderColor = borderColor.copy(0.7f),
        borderRadius = 8
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(surfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = provider.type.initials,
                    color = dawsonGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(textPrimaryColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)) { append(provider.type.label + "\n") }
                    withStyle(style = SpanStyle(textSecondaryColor, fontSize = 12.sp, fontWeight = FontWeight.Normal)) {
                        append("${provider.models.count()} Model(s)")
                    }
                },
                lineHeight = 17.sp,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (needsConfig) dawsonRed else darkGreenColor)
                    .padding(horizontal = 8.dp)
                    .height(19.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = if (needsConfig) "Not Configured" else "Ready",
                    color = if (needsConfig) dangerColor else lightGreenColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .height(40.dp)
                    .width(1.dp)
                    .background(borderColor)
            )

            Icon(
                modifier = Modifier.padding(start = 8.dp),
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = textPrimaryColor
            )
        }
    }
}