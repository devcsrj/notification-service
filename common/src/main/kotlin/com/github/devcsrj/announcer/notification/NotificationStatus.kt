package com.github.devcsrj.announcer.notification

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
sealed class NotificationStatus

object Pending : NotificationStatus()

data class Sent(val timestamp: LocalDateTime) : NotificationStatus() {

    companion object {
        fun justNow() = Sent(LocalDateTime.now())
    }
}

data class SendingFailed(
    val timestamp: LocalDateTime,
    val reason: String
) : NotificationStatus() {

    companion object {
        fun justNow(because: String) =
            SendingFailed(LocalDateTime.now(), because)
    }
}
