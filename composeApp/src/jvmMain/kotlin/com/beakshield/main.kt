package com.beakshield

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.awt.Taskbar
import java.awt.Toolkit

fun main() = application {
    val windowState = rememberWindowState(
        // TODO: Remove these once adaptive setup
        placement = WindowPlacement.Floating
    )

    setDesktopIcon()

    Window(
        onCloseRequest = ::exitApplication,
        title = "BeakShield",
        resizable = true,
        state = windowState
    ) {
        window.minimumSize = Dimension(1000, 950)

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