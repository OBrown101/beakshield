package com.beakshield

import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Toolkit
import java.io.File
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.UIManager

actual val isJvm: Boolean = true

var lastDir: File? = null

actual fun pickFilePath(): String? {
    // Swing components must be created and shown on the EDT. Compose/KMP
    // callers are often on a different thread, which by itself can cause
    // flaky mouse handling in JFileChooser.
    var result: String? = null
    val task = Runnable { result = showPickerDialog() }
    if (SwingUtilities.isEventDispatchThread()) task.run()
    else SwingUtilities.invokeAndWait(task)
    return result
}

private fun showPickerDialog(): String? {
    // JFileChooser has an inline-rename editor: a click on an already-selected
    // item starts editing its name. The second click of a double-click often
    // lands on the now-selected folder and opens the rename editor instead of
    // navigating -- that's the intermittent "won't open the folder" glitch.
    // Marking the chooser read-only disables that editor entirely.
    UIManager.put("FileChooser.readOnly", true)

    var selectedPath: String? = null

    val chooser = object : JFileChooser(lastDir) {
        override fun approveSelection() {
            val selected = selectedFile
            when {
                // Double-clicking a directory navigates into it.
                selected?.isDirectory == true -> {
                    currentDirectory = selected // this also triggers a rescan
                    // Clear the stale selection so it can't interfere with the
                    // next double-click or with the Select button.
                    selectedFile = null
                }

                // Double-clicking a file selects it.
                selected?.isFile == true -> {
                    selectedPath = selected.absolutePath
                    lastDir = selected
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
                        if (selected != null && (selected.isFile || selected.isDirectory)) {
                            selectedPath = selected.absolutePath
                            lastDir = selected
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