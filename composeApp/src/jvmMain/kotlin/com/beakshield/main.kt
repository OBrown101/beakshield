package com.beakshield

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Taskbar
import java.awt.Toolkit

fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Floating,
        width = 950.dp,
        height = 900.dp
    )

    setDesktopIcon()

    Window(
        onCloseRequest = ::exitApplication,
        title = "BeakShield",
        resizable = true,
        state = windowState
    ) {

        App()
    }
}

fun setDesktopIcon() {
    if (Taskbar.isTaskbarSupported()) {
        val image = Toolkit.getDefaultToolkit()
            .getImage(Thread.currentThread().contextClassLoader.getResource("icon_beakshield.png"))

        Taskbar.getTaskbar().iconImage = image
    }
}