package com.beakshield

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual val isJvm: Boolean = true

actual suspend fun pickFilePath(): String? {
    System.setProperty("apple.awt.fileDialogForDirectories", "true")

    val frame = Frame()
    val dialog = FileDialog(frame, "Select directory", FileDialog.LOAD)

    dialog.isVisible = true

    val selectedDirectory = dialog.directory
    val selectedFile = dialog.file

    dialog.dispose()
    frame.dispose()

    System.setProperty("apple.awt.fileDialogForDirectories", "false")

    return when {
        selectedDirectory == null -> null
        selectedFile != null -> File(selectedDirectory, selectedFile).absolutePath
        else -> selectedDirectory
    }
}