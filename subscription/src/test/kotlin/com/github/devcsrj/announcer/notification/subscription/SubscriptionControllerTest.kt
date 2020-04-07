package com.github.devcsrj.announcer.notification.subscription

import com.github.devcsrj.announcer.customer.CustomerId
import org.assertj.core.api.Assertions.assertThat
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
import java.net.URI

@ExtendWith(SpringExtension::class)
@Import(SubscriptionControllerTest.TestModule::class)
@WebFluxTest(value = [SubscriptionController::class])
class SubscriptionControllerTest {

    class TestModule {

        @Bean
        internal fun subscriptionRepository(): SubscriptionRepository {
            return InMemorySubscriptionRepository()
        }

        @Bean
        internal fun subscriptionService(subscriptionRepository: SubscriptionRepository): SubscriptionService {
            return DefaultSubscriptionService(subscriptionRepository)
        }
    }

    @Autowired
    lateinit var webClient: WebTestClient

    @Test
    fun `customer can subscribe with url`() {
        val customerId = "sheldon"
        val result = webClient.post().uri("/api/subscriptions/$customerId")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue(
                    """
                    { "callbackUrl": "https://example.com" }
                """.trimIndent()
                )
            )
            .exchange()
            .expectStatus().isOk
            .returnResult(Subscription::class.java)

        StepVerifier.create(result.responseBody)
            .expectNextMatches {
                assertThat(it.id).isNotNull
                assertThat(it.customerId).isEqualTo(CustomerId(customerId))
                assertThat(it.callbackUrl).isEqualTo(URI.create("https://example.com"))
                true
            }
            .verifyComplete()
    }
}