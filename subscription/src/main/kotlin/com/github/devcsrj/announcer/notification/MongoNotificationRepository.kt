package com.github.devcsrj.announcer.notification

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class MongoNotificationRepository(private val dao: Dao) : NotificationRepository {

    override fun save(notification: Notification): Mono<Notification> = dao.save(notification)
    override fun findById(notificationId: NotificationId): Mono<Notification> = dao.findById(notificationId)
    override fun findByRecipientId(recipientId: RecipientId) = dao.findByRecipientIdOrderByCreatedDesc(recipientId)

    @Repository("mongoNotificationDao")
    interface Dao : ReactiveMongoRepository<Notification, NotificationId> {
        fun findByRecipientIdOrderByCreatedDesc(recipientId: RecipientId): Flux<Notification>
    }
}