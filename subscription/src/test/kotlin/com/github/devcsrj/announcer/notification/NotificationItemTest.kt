package com.github.devcsrj.announcer.notification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class NotificationItemTest {

    private lateinit var notification: Notification

    @BeforeEach
    fun beforeEach() {
        notification = Notification(
            id = NotificationId("bbt-0"),
            recipientId = RecipientId("sheldon"),
            created = LocalDateTime.now(),
            payload = mapOf("knock" to 3),
            status = Pending,
            type = NotificationType("welcome")
        )
    }

    @Test
    fun `convert from pending notification`() {
        val actual = NotificationItem.from(notification)
        assertThat(actual.id).isEqualTo(notification.id.value)
        assertThat(actual.type).isEqualTo(notification.type.value)
        assertThat(actual.created).isEqualTo(notification.created)
        assertThat(actual.payload).isEqualTo(notification.payload)
        assertThat(actual.status).isEqualTo("Pending")
        assertThat(actual.message).isEqualTo("Pending")
    }

    @Test
    fun `convert from sent notification`() {
        val actual = NotificationItem.from(
            notification.copy(status = Sent.justNow())
        )
        assertThat(actual.id).isEqualTo(notification.id.value)
        assertThat(actual.type).isEqualTo(notification.type.value)
        assertThat(actual.created).isEqualTo(notification.created)
        assertThat(actual.payload).isEqualTo(notification.payload)
        assertThat(actual.status).isEqualTo("Sent")
        assertThat(actual.message).isEqualTo("OK")
    }

    @Test
    fun `convert from failed notification`() {
        val actual = NotificationItem.from(
            notification.copy(status = SendingFailed.justNow("That's my spot"))
        )
        assertThat(actual.id).isEqualTo(notification.id.value)
        assertThat(actual.type).isEqualTo(notification.type.value)
        assertThat(actual.created).isEqualTo(notification.created)
        assertThat(actual.payload).isEqualTo(notification.payload)
        assertThat(actual.status).isEqualTo("SendingFailed")
        assertThat(actual.message).isEqualTo("That's my spot")
    }
}