package com.beakshield.viewModels

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class RailContent(
    val content: (@Composable (Modifier) -> Unit)?,
    val width: Int
)
