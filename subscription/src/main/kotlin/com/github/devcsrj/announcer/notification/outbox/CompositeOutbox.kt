package com.github.devcsrj.announcer.notification.outbox

import com.github.devcsrj.announcer.notification.Notification
import com.github.devcsrj.announcer.notification.NotificationId
import com.github.devcsrj.announcer.notification.NotificationStatus
import com.github.devcsrj.announcer.notification.RecipientId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class CompositeOutbox(private val outboxes: List<Outbox>) : Outbox {

    override fun send(notification: Notification): Mono<Void> {
        return Flux.fromIterable(outboxes)
            .flatMap { it.send(notification) }
            .then()
    }

    override fun update(notificationId: NotificationId, status: NotificationStatus): Mono<Void> {
        return Flux.fromIterable(outboxes)
            .flatMap { it.update(notificationId, status) }
            .then()
    }

    override fun contents(recipientId: RecipientId): Flux<Notification> {
        return Flux.fromIterable(outboxes).flatMap { it.contents(recipientId) }
    }
}