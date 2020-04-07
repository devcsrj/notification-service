package com.github.devcsrj.announcer.notification

import com.github.devcsrj.announcer.notification.outbox.Outbox
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping(
    value = ["/api/notifications"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class NotificationController(
    val outbox: Outbox
) {

    @PostMapping(value = ["/send"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun send(
        @RequestBody body: SendNotificationRequest,
        response: ServerHttpResponse
    ): Mono<SendNotificationResponse> {

        val recipientId = RecipientId(body.customerId)
        val notification = Notification.create(recipientId,
            NotificationType(body.type), body.payload)
        val sent = outbox.send(notification)
            .then(Mono.just(SendNotificationResponse(messageId = notification.id.value)))
        response.headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
        response.statusCode = HttpStatus.ACCEPTED

        return sent
    }

    @PutMapping(value = ["/{notificationId}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sent(
        @PathVariable notificationId: String,
        @RequestBody body: NotificationAcknowledgementRequest,
        response: ServerHttpResponse
    ): Mono<Void> {

        val update = outbox.update(
            NotificationId(
                notificationId
            ), body.status)
        response.headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
        response.statusCode = HttpStatus.ACCEPTED

        return update
    }

    @GetMapping(params = ["customerId"])
    fun getByCustomer(@RequestParam customerId: String): Flux<NotificationItem> {

        val recipientId = RecipientId(customerId)
        return outbox
            .contents(recipientId)
            .map { NotificationItem.from(it) }
    }
}