package com.github.devcsrj.announcer.notification

import com.github.devcsrj.announcer.notification.outbox.InMemoryOutbox
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.test.StepVerifier

@ExtendWith(SpringExtension::class)
@Import(NotificationControllerTest.TestModule::class)
@WebFluxTest(value = [NotificationController::class])
class NotificationControllerTest {

    class TestModule {
        @Bean
        fun outbox(): InMemoryOutbox {
            return InMemoryOutbox()
        }
    }

    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    lateinit var outbox: InMemoryOutbox

    @Test
    fun `can send notification`() {
        val notification = SendNotificationRequest(
            "sheldon", "greeting", mapOf("knock" to 3)
        )

        val result = webClient.post().uri("/api/notifications/send")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(notification))
            .exchange()
            .expectStatus().isAccepted
            .returnResult(SendNotificationResponse::class.java)

        StepVerifier.create(result.responseBody)
            .expectNextMatches {
                assertNotNull(it.messageId)
                true
            }
            .verifyComplete()

        assertThat(outbox.notifications).hasSize(1)
        val actual = outbox.notifications.values.first()
        assertThat(actual.id).isNotNull
        assertThat(actual.recipientId.value).isEqualTo("sheldon")
        assertThat(actual.payload).isEqualTo(mapOf("knock" to 3))
        assertThat(actual.type).isEqualTo(
            NotificationType(
                "greeting"
            )
        )
    }

    @Test
    fun `can update notification`() {
        val status = Sent.justNow()
        val update = NotificationAcknowledgementRequest(status)

        val notification = Notification.create(
            RecipientId("sheldon"),
            NotificationType("greeting"), mapOf()
        )
        outbox.send(notification).block()

        webClient.put().uri("/api/notifications/${notification.id.value}")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(update))
            .exchange()
            .expectStatus().isAccepted


        val actual = outbox.notifications[notification.id]!!
        assertThat(actual.status).isEqualTo(status)
    }

    @Test
    fun `can retrieve notifications`() {
        val notification = Notification.create(
            RecipientId("sheldon"),
            NotificationType("greeting"), mapOf()
        )
        outbox.send(notification).block()

        val result = webClient.get().uri("/api/notifications?customerId=sheldon")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .returnResult(NotificationItem::class.java)

        val actual = result.responseBody.collectList().block()
        assertThat(actual).hasSizeGreaterThan(0)
    }
}