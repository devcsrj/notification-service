package com.github.devcsrj.announcer.notification.sender

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.devcsrj.announcer.notification.*
import com.github.devcsrj.announcer.notification.subscription.SubscriptionService
import com.github.devcsrj.announcer.user.UserService
import com.github.devcsrj.announcer.user.token.Token
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI

class NotificationCallbackHandler(
    private val subscriptionSvcUrl: URI,
    private val objectMapper: ObjectMapper,
    private val httpClient: OkHttpClient
) : OutboxHandler {

    companion object {
        private const val HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key"
        private val JSON = "application/json".toMediaTypeOrNull()
        private val LOGGER = LoggerFactory.getLogger(NotificationCallbackHandler::class.java)
    }

    private val notificationService: NotificationService
    private val subscriptionService: SubscriptionService
    private val userService: UserService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(subscriptionSvcUrl.toURL())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .client(httpClient)
            .build()
        notificationService = retrofit.create(NotificationService::class.java)
        subscriptionService = retrofit.create(SubscriptionService::class.java)
        userService = retrofit.create(UserService::class.java)
    }

    constructor(subscriptionSvcUrl: URI, objectMapper: ObjectMapper) : this(
        subscriptionSvcUrl,
        objectMapper,
        OkHttpClient()
    )

    @StreamListener(Sink.INPUT)
    fun onReceive(
        @Payload notification: Notification,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partition: Int
    ) {
        handle(notification)
    }

    override fun handle(notification: Notification) {
        val recipient = notification.recipientId
        try {
            val callbackUrl = getCallbackUrlOf(recipient)
            doCallback(callbackUrl, notification)
            updateNotification(notification.id, Sent.justNow())
        } catch (e: CallbackException) {
            updateNotification(notification.id, SendingFailed.justNow(e.message!!))
            throw e
        }
    }

    private fun updateNotification(id: NotificationId, status: NotificationStatus) {
        val service = notificationService.update(id.value, UpdateNotificationRequest(status)).execute()
        if (service.isSuccessful) {
            LOGGER.info("Notification '${id.value}' updated to '$status")
        } else {
            LOGGER.error("Failed to update notification '${id.value}' to '$status")
        }
    }

    private fun doCallback(url: URI, notif: Notification) {
        val token = getTokenOf(notif.recipientId)
        val content = objectMapper.writeValueAsBytes(notif.payload)
        val request = Request.Builder()
            .post(content.toRequestBody(JSON, 0, content.size))
            .url(url.toURL())
            .header(HEADER_IDEMPOTENCY_KEY, notif.id.value)
            .header("Authorization", "Bearer ${token.value}")
            .build()
        try {
            httpClient.newCall(request).execute().use {
                if (!it.isSuccessful) {
                    throw CallbackException(
                        "The callback url '${url}' returned '${it.code}'"
                    )
                }
            }
        } catch (e: SocketTimeoutException) {
            throw CallbackException("The callback url '$url' timed out")
        } catch (e: ConnectException) {
            throw CallbackException("The callback url '$url' is unreachable")
        } catch (e: Exception) {
            throw CallbackException("Could not POST to callback url '$url' (${e.message})")
        }
    }

    private fun getCallbackUrlOf(recipientId: RecipientId): URI {
        val response = subscriptionService.getSubscription(recipientId.value).execute()
        if (response.isSuccessful) {
            return response.body()!!.callbackUrl
        }
        throw CallbackException(
            "The service '${subscriptionSvcUrl}' did not return a subscription payload for recipient '${recipientId.value}'"
        )
    }

    private fun getTokenOf(recipientId: RecipientId): Token {
        val response = userService.getTokens(recipientId.value).execute()
        var token = Token("Unset", "Unset")
        if (response.isSuccessful) {
            val tokens = response.body() ?: emptyList()
            if (tokens.isNotEmpty()) {
                token = tokens.first()
            }
        }
        return token
    }
}