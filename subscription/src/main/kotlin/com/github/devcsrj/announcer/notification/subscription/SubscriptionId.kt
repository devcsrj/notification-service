package com.github.devcsrj.announcer.notification.subscription

import java.util.*

data class SubscriptionId(val value: String) {

    companion object {
        fun create(): SubscriptionId {
            return SubscriptionId(UUID.randomUUID().toString())
        }
    }
}