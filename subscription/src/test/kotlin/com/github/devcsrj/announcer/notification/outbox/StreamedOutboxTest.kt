package com.github.devcsrj.announcer.notification.outbox

import com.github.devcsrj.announcer.notification.*
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import java.time.LocalDateTime

internal class StreamedOutboxTest {


    @Test
    fun `can send`() {
        val notification = Notification(
            id = NotificationId("bbt-0"),
            recipientId = RecipientId("sheldon"),
            created = LocalDateTime.now(),
            payload = mapOf("knock" to 3),
            status = Pending,
            type = NotificationType("welcome")
        )
        val channel = mock<MessageChannel> {
            on { send(any()) } doReturn true
        }
        val source = mock<OutboxSource> {
            on { output() } doReturn channel
        }

        val outbox = StreamedOutbox(source)
        outbox.send(notification).block()

        val captor = ArgumentCaptor.forClass(Message::class.java)
        verify(channel).send(captor.capture())

        val actual = captor.value
        assertThat(actual.headers["recipientId"]).isEqualTo("welcome:sheldon")
        assertThat(actual.payload).isEqualTo(notification)
    }
}