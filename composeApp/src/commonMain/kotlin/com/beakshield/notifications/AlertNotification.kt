package org.legionarius.vector.notifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.beakshield.dangerColor
import com.beakshield.infoColor
import com.beakshield.lightGreenColor
import com.beakshield.notifications.AlertButton
import com.beakshield.primaryColor
import com.beakshield.warningColor

data class AlertNotification(
    val type: AlertType = AlertType.POPUP,
    val icon: AlertIcon = AlertIcon.WARNING,
    val title: String,
    val message: String = "",
    val detailMessage: String? = null,
    val buttons: List<AlertButton> = emptyList()
) {
    enum class AlertType {
        POPUP,
        BANNER
    }

    enum class AlertIcon {
        INFO,
        WARNING,
        ERROR,
        SUCCESS,
        QUESTION,
        NONE;

        val imageVector: ImageVector?
            get() = when (this) {
                INFO -> Icons.Rounded.Info
                WARNING -> Icons.Rounded.WarningAmber
                ERROR -> Icons.Rounded.ErrorOutline
                SUCCESS -> Icons.Rounded.CheckCircle
                QUESTION -> Icons.AutoMirrored.Rounded.HelpOutline
                NONE -> null
            }

        val color: Color
            get() = when (this) {
                INFO -> infoColor
                WARNING -> warningColor
                ERROR -> dangerColor
                SUCCESS -> lightGreenColor
                QUESTION -> primaryColor
                NONE -> Color.Transparent
            }
    }

    object MockAlert {
        val mockAlerts = listOf(
            AlertNotification(
                icon = AlertIcon.WARNING,
                title = "Delete Chat",
                message = "This chat and all associated messages will be permanently deleted.",
                detailMessage = "This action cannot be undone.",
                buttons = listOf(
                    AlertButton("Cancel",
                        onClick = {}
                    ),
                    AlertButton("Delete", AlertButton.ButtonStyle.DANGER,
                        onClick = {}
                    )
                )
            ),

            AlertNotification(
                icon = AlertIcon.QUESTION,
                title = "Logout",
                message = "Are you sure you want to sign out of this account?",
                buttons = listOf(
                    AlertButton("Stay Signed In",
                        onClick = {}
                    ),
                    AlertButton("Logout", AlertButton.ButtonStyle.PRIMARY,
                        onClick = {}
                    )
                )
            ),

            AlertNotification(
                icon = AlertIcon.ERROR,
                title = "Connection Failed",
                message = "Unable to establish a connection to the DAWSON server.",
                detailMessage = "Verify the IP address, port, and that the server is currently running.",
                buttons = listOf(
                    AlertButton("Dismiss",
                        onClick = {}
                    ),
                    AlertButton(
                        "Retry", AlertButton.ButtonStyle.PRIMARY,
                        onClick = {}
                    )
                )
            ),

            AlertNotification(
                icon = AlertIcon.SUCCESS,
                title = "Upload Complete",
                message = "Your files have been successfully uploaded.",
                buttons = listOf(
                    AlertButton("Done", AlertButton.ButtonStyle.PRIMARY,
                        onClick = {}
                    )
                )
            ),

            AlertNotification(
                icon = AlertIcon.INFO,
                title = "Software Update Available",
                message = "Version 1.8.2 is available and ready to install.",
                detailMessage = "The update includes performance improvements, bug fixes, and new AI capabilities.",
                buttons = listOf(
                    AlertButton("Later",
                        onClick = {}
                    ),
                    AlertButton("Install", AlertButton.ButtonStyle.PRIMARY,
                        onClick = {}
                    )
                )
            )
        )
    }
}