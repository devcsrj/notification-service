package com.github.devcsrj.announcer.notification.outbox

import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel

interface OutboxSource {

    companion object {
        const val RECIPIENT = "recipientId"
        private const val OUTPUT = "outbox"
    }

    @Output(OUTPUT)
    fun output(): MessageChannel
}