package com.github.devcsrj.announcer.notification.outbox

import com.github.devcsrj.announcer.notification.Notification
import com.github.devcsrj.announcer.notification.NotificationId
import com.github.devcsrj.announcer.notification.NotificationStatus
import com.github.devcsrj.announcer.notification.RecipientId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class InMemoryOutbox : Outbox {

    internal val notifications = mutableMapOf<NotificationId, Notification>()

    override fun send(notification: Notification): Mono<Void> {
        return Mono.create {
            notifications[notification.id] = notification
            it.success()
        }
    }

    override fun update(notificationId: NotificationId, status: NotificationStatus): Mono<Void> {
        return Mono.create {
            val existing = notifications[notificationId]
            if (existing != null) {
                notifications[notificationId] = existing.copy(status = status)
            }
            it.success()
        }
    }

    override fun contents(recipientId: RecipientId): Flux<Notification> {
        return Flux.fromIterable(notifications.values)
            .filter { it.recipientId == recipientId }
    }
}