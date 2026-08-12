package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beakshield.dawsonGold
import com.beakshield.textSecondaryColor

@Composable
fun KnowledgeLoadingContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isEmpty: Boolean,
    emptyMessage: String,
    content: @Composable (Modifier) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                color = dawsonGold,
                strokeWidth = 2.dp
            )
            isEmpty -> Text(
                text = emptyMessage,
                color = textSecondaryColor
            )
            else -> content(Modifier.fillMaxSize())
        }
    }
}
