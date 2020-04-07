package com.github.devcsrj.announcer.notification

import java.time.LocalDateTime

data class NotificationItem(
    val id: String,
    val type: String,
    val created: LocalDateTime,
    val payload: Map<String, Any>,
    val status: String,
    val message: String?
) {

    companion object {

        fun from(notification: Notification): NotificationItem {
            val status: String
            val message: String
            when (val n = notification.status) {
                is Pending -> {
                    status = "Pending"
                    message = "Pending"
                }
                is Sent -> {
                    status = "Sent"
                    message = "OK"
                }
                is SendingFailed -> {
                    status = "SendingFailed"
                    message = n.reason
                }
                else -> {
                    status = notification.status::class.simpleName!!
                    message = "Unknown"
                }
            }

            return NotificationItem(
                id = notification.id.value,
                type = notification.type.value,
                created = notification.created,
                payload = notification.payload,
                status = status,
                message = message
            )
        }
    }
}