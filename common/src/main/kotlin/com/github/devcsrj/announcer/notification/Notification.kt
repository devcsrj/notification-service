package com.github.devcsrj.announcer.notification

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class Notification(
    @Id
    val id: NotificationId,
    val recipientId: RecipientId,
    val created: LocalDateTime,
    val payload: Map<String, Any>,
    val status: NotificationStatus,
    val type: NotificationType
) {

    companion object {

        fun create(
            recipientId: RecipientId,
            type: NotificationType,
            payload: Map<String, Any>
        ): Notification {
            val id = NotificationId.create()
            return Notification(
                id = id,
                recipientId = recipientId,
                created = LocalDateTime.now(),
                payload = payload,
                status = Pending,
                type = type
            )
        }
    }
}