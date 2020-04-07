package com.github.devcsrj.announcer.notification.subscription

import java.net.URI

data class SubscriptionRequest(
    val callbackUrl: URI
)