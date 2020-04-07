package com.github.devcsrj.announcer.notification.outbox

import com.github.devcsrj.announcer.notification.Notification
import com.github.devcsrj.announcer.notification.NotificationId
import com.github.devcsrj.announcer.notification.NotificationStatus
import com.github.devcsrj.announcer.notification.RecipientId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface Outbox {

    fun send(notification: Notification): Mono<Void>

    fun update(notificationId: NotificationId, status: NotificationStatus): Mono<Void>

    fun contents(recipientId: RecipientId): Flux<Notification>
}