package com.github.devcsrj.announcer.notification.sender

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.devcsrj.announcer.notification.Notification
import com.github.devcsrj.announcer.notification.NotificationType
import com.github.devcsrj.announcer.notification.RecipientId
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.concurrent.TimeUnit
import com.revinate.assertj.json.JsonPathAssert.assertThat as assertJsonThat

internal class NotificationCallbackHandlerTest {

    companion object {
        private val mapper = ObjectMapper().apply {
            registerModule(KotlinModule())
            registerModule(JavaTimeModule())
            configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    private lateinit var subscriptionSvc: MockWebServer
    private lateinit var callbackSvc: MockWebServer

    @BeforeEach
    fun beforeEach() {
        subscriptionSvc = MockWebServer()
        callbackSvc = MockWebServer()
    }

    @Test
    fun `can handle golden path`() {
        subscriptionSvc.enqueue(
            MockResponse()
                .setResponseCode(202)
                .setBody("{\"callbackUrl\": \"${callbackSvc.url("/payments")}\"}")
        )
        subscriptionSvc.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[{\"value\":\"qwerty\", \"name\":\"notifications\"}]")
        )
        subscriptionSvc.enqueue(MockResponse().setResponseCode(204))
        callbackSvc.enqueue(MockResponse().setResponseCode(204))

        val notification = Notification.create(
            RecipientId("sheldon"),
            NotificationType("welcome"), mapOf("knock" to 3)
        )
        val subSvcUrl = URI.create(subscriptionSvc.url("/").toString())
        val handler = NotificationCallbackHandler(subSvcUrl, mapper)
        handler.handle(notification)

        var rr = subscriptionSvc.takeRequest(500L, TimeUnit.MILLISECONDS) ?: fail("missing subscription request")
        assertThat(rr.path).isEqualTo("/api/subscriptions/sheldon")
        assertThat(rr.method).isEqualTo("GET")

        rr = subscriptionSvc.takeRequest(500L, TimeUnit.MILLISECONDS) ?: fail("missing tokens request")
        assertThat(rr.path).isEqualTo("/api/users/sheldon/tokens")
        assertThat(rr.method).isEqualTo("GET")

        rr = callbackSvc.takeRequest(500L, TimeUnit.MILLISECONDS) ?: fail("missing callback request")
        assertThat(rr.path).isEqualTo("/payments")
        assertThat(rr.method).isEqualTo("POST")
        assertThat(rr.headers["X-Idempotency-Key"]).isEqualTo(notification.id.value)
        assertThat(rr.headers["Authorization"]).isEqualTo("Bearer qwerty")
        var ctx = JsonPath.parse(rr.body.readUtf8())
        assertJsonThat(ctx).jsonPath("$.knock").isEqualTo(3)

        rr = subscriptionSvc.takeRequest(500L, TimeUnit.MILLISECONDS) ?: fail("missing notif update request")
        assertThat(rr.path).isEqualTo("/api/notifications/${notification.id.value}")
        assertThat(rr.method).isEqualTo("PUT")
        ctx = JsonPath.parse(rr.body.readUtf8())
        assertJsonThat(ctx).jsonPathAsString("$.status.timestamp").isNotBlank()
        try {
            assertJsonThat(ctx).jsonPath("$.status.reason")
            fail("Unexpected 'reason' from notification update payload") as Nothing
        } catch (e: PathNotFoundException) {
            // expected
        }
    }

    @Test
    fun `can handle customer not having subscription`() {
        subscriptionSvc.enqueue(MockResponse().setResponseCode(404)) // no subscription
        subscriptionSvc.enqueue(MockResponse().setResponseCode(204))

        val notification = Notification.create(
            RecipientId("sheldon"),
            NotificationType("welcome"), mapOf("knock" to 3)
        )
        val subSvcUrl = URI.create(subscriptionSvc.url("/").toString())
        val handler = NotificationCallbackHandler(subSvcUrl, mapper)
        try {
            handler.handle(notification)
            fail("exception should be propagated") as Nothing
        } catch (e: CallbackException) {
            // expected
        }

        var rr = subscriptionSvc.takeRequest(500L, TimeUnit.MILLISECONDS) ?: fail("missing subscription request")
        assertThat(rr.path).isEqualTo("/api/subscriptions/sheldon")
        assertThat(rr.method).isEqualTo("GET")

        rr = subscriptionSvc.takeRequest(500L, TimeUnit.MILLISECONDS) ?: fail("missing notif update request")
        assertThat(rr.path).isEqualTo("/api/notifications/${notification.id.value}")
        assertThat(rr.method).isEqualTo("PUT")
        val ctx = JsonPath.parse(rr.body.readUtf8())
        assertJsonThat(ctx).jsonPathAsString("$.status.timestamp").isNotBlank()
        assertJsonThat(ctx).jsonPathAsString("$.status.reason").isEqualTo(
            "The service '${subSvcUrl}' did not return a subscription payload for recipient 'sheldon'"
        )
    }

    @Test
    fun `can handle customer server timed out`() {
        subscriptionSvc.enqueue(
            MockResponse()
                .setResponseCode(202)
                .setBody("{\"callbackUrl\": \"${callbackSvc.url("/payments")}\"}")
        )
        subscriptionSvc.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[{\"value\":\"qwerty\", \"name\":\"notifications\"}]")
        )
        subscriptionSvc.enqueue(MockResponse().setResponseCode(204))
        callbackSvc.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val notification = Notification.create(
            RecipientId("sheldon"),
            NotificationType("welcome"), mapOf("knock" to 3)
        )
        val subSvcUrl = URI.create(subscriptionSvc.url("/").toString())
        val httpClient = OkHttpClient.Builder()
            .readTimeout(250L, TimeUnit.MILLISECONDS)
            .build()
        val handler = NotificationCallbackHandler(subSvcUrl, mapper, httpClient)
        try {
            handler.handle(notification)
            fail("exception should be propagated") as Nothing
        } catch (e: CallbackException) {
            // expected
        }

        var rr = subscriptionSvc.takeRequest(500L, TimeUnit.MILLISECONDS) ?: fail("missing subscription request")
        assertThat(rr.path).isEqualTo("/api/subscriptions/sheldon")
        assertThat(rr.method).isEqualTo("GET")

        rr = subscriptionSvc.takeRequest(500L, TimeUnit.MILLISECONDS) ?: fail("missing tokens request")
        assertThat(rr.path).isEqualTo("/api/users/sheldon/tokens")
        assertThat(rr.method).isEqualTo("GET")

        rr = subscriptionSvc.takeRequest(500L, TimeUnit.MILLISECONDS) ?: fail("missing notif update request")
        assertThat(rr.path).isEqualTo("/api/notifications/${notification.id.value}")
        assertThat(rr.method).isEqualTo("PUT")
        val ctx = JsonPath.parse(rr.body.readUtf8())
        assertJsonThat(ctx).jsonPathAsString("$.status.timestamp").isNotBlank()
        assertJsonThat(ctx).jsonPathAsString("$.status.reason").isEqualTo(
            "The callback url '${callbackSvc.url("/payments")}' timed out"
        )
    }
}