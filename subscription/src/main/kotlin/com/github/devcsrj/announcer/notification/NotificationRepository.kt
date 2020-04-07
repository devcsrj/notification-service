package com.github.devcsrj.announcer.notification

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface NotificationRepository {

    fun save(notification: Notification): Mono<Notification>

    fun findById(notificationId: NotificationId): Mono<Notification>

    fun findByRecipientId(recipientId: RecipientId): Flux<Notification>
}