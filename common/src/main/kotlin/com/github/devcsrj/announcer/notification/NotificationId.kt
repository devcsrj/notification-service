package com.github.devcsrj.announcer.notification

import java.io.Serializable
import java.util.*

data class NotificationId(val value: String) : Serializable {

    companion object {

        fun create(): NotificationId {
            return NotificationId(
                UUID.randomUUID().toString()
            )
        }
    }
}