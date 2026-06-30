package com.beakshield

import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Toolkit
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JPanel

actual val isJvm: Boolean = true

actual fun pickFilePath(): String? {
    var selectedPath: String? = null

    val chooser = object : JFileChooser() {
        override fun approveSelection() {
            val selected = selectedFile

            when {
                // Double-clicking a directory navigates into it.
                selected?.isDirectory == true -> {
                    currentDirectory = selected
                    rescanCurrentDirectory()
                }

                // Double-clicking a file selects it.
                selected?.isFile == true -> {
                    selectedPath = selected.absolutePath
                    super.approveSelection()
                }

                else -> Toolkit.getDefaultToolkit().beep()
            }
        }
    }.apply {
        dialogTitle = "Select File or Directory"
        fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        isAcceptAllFileFilterUsed = true
        controlButtonsAreShown = false
    }

    lateinit var dialog: JDialog

    dialog = JDialog().apply {
        title = chooser.dialogTitle
        isModal = true
        defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
        layout = BorderLayout()

        add(chooser, BorderLayout.CENTER)

        add(
            JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
                add(JButton("Cancel").apply {
                    addActionListener {
                        selectedPath = null
                        dialog.dispose()
                    }
                })

                add(JButton("Select").apply {
                    addActionListener {
                        val selected = chooser.selectedFile

                        if (selected?.isFile == true || selected?.isDirectory == true) {
                            selectedPath = selected.absolutePath
                            dialog.dispose()
                        } else {
                            Toolkit.getDefaultToolkit().beep()
                        }
                    }
                })
            },
            BorderLayout.SOUTH
        )

        chooser.addActionListener {
            when (it.actionCommand) {
                JFileChooser.APPROVE_SELECTION -> dialog.dispose()
                JFileChooser.CANCEL_SELECTION -> {
                    selectedPath = null
                    dialog.dispose()
                }
            }
        }

        pack()
        setLocationRelativeTo(null)
    }

    dialog.isVisible = true

    return selectedPath
}