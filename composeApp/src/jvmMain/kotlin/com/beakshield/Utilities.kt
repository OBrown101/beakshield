package com.beakshield

import javax.swing.JFileChooser

actual val isJvm: Boolean = true

actual suspend fun pickFilePath(): String? {
    val chooser = JFileChooser().apply {
        dialogTitle = "Select file or directory"
        fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        isAcceptAllFileFilterUsed = true
    }

    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile?.absolutePath
    } else {
        null
    }
}

