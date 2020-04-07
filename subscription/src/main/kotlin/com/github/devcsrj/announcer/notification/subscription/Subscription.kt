package com.github.devcsrj.announcer.notification.subscription

import com.github.devcsrj.announcer.customer.CustomerId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import java.net.URI
import java.time.LocalDateTime

data class Subscription(
    @Id
    val id: SubscriptionId,
    @Indexed(unique = true)
    val customerId: CustomerId,
    val date: LocalDateTime,
    val callbackUrl: URI
)