package com.beakshield.notifications

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.legionarius.vector.notifications.AlertNotification

class Notifications {

    private val _alerts = MutableSharedFlow<AlertNotification>(
        extraBufferCapacity = 50,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val alerts: SharedFlow<AlertNotification> = _alerts.asSharedFlow()

    fun sendAlertRequest(
        type: AlertNotification.AlertType = AlertNotification.AlertType.POPUP,
        icon: AlertNotification.AlertIcon = AlertNotification.AlertIcon.WARNING,
        title: String,
        message: String = "",
        detailMessage: String? = null,
        buttons: List<AlertButton> = emptyList()
    ) {
        val alert = AlertNotification(
            type = type,
            icon = icon,
            title = title,
            message = message,
            detailMessage = detailMessage,
            buttons = buttons
        )

        _alerts.tryEmit(alert)
    }

    fun sendAlertRequest(alert: AlertNotification) {
        _alerts.tryEmit(alert)
    }
}