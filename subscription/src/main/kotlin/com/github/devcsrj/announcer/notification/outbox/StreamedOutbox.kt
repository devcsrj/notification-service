package com.github.devcsrj.announcer.notification.outbox

import com.github.devcsrj.announcer.notification.Notification
import com.github.devcsrj.announcer.notification.NotificationId
import com.github.devcsrj.announcer.notification.NotificationStatus
import com.github.devcsrj.announcer.notification.RecipientId
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@EnableBinding(OutboxSource::class)
class StreamedOutbox(private val source: OutboxSource) : Outbox {

    override fun send(notification: Notification): Mono<Void> {
        return Mono.create {
            val recipientId = notification.recipientId
            val type = notification.type
            val key = "${type.value}:${recipientId.value}"
            val sent = source.output().send(
                MessageBuilder.withPayload(notification)
                    .setHeader(OutboxSource.RECIPIENT, key)
                    .build()
            )
            if (sent) {
                it.success()
            } else {
                it.error(RuntimeException("Could not send message to '${recipientId.value}'"))
            }
        }
    }

    override fun update(notificationId: NotificationId, status: NotificationStatus): Mono<Void> = Mono.empty()
    override fun contents(recipientId: RecipientId): Flux<Notification> = Flux.empty()
}