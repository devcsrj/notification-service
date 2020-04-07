package com.github.devcsrj.announcer.notification.outbox

import com.github.devcsrj.announcer.notification.*
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class PersistingOutbox(private val notificationRepository: NotificationRepository) : Outbox {

    @Transactional
    override fun send(notification: Notification): Mono<Void> {
        return Mono.just(notification)
            .flatMap { notificationRepository.save(it) }
            .then()
    }

    @Transactional
    override fun update(notificationId: NotificationId, status: NotificationStatus): Mono<Void> {
        return notificationRepository.findById(notificationId)
            .map { it.copy(status = status) }
            .flatMap { notificationRepository.save(it) }
            .then()
    }

    @Transactional
    override fun contents(recipientId: RecipientId): Flux<Notification> {
        return notificationRepository.findByRecipientId(recipientId)
    }
}