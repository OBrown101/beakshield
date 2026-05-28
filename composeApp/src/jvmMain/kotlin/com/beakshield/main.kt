package com.beakshield

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(
        // TODO: Remove these once adaptive setup
        width = 640.dp,
        height = 800.dp,
        placement = WindowPlacement.Floating
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "BeakShield",
        resizable = false,
        state = windowState
    ) {
        App()
    }
}