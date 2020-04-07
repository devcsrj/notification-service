package com.github.devcsrj.announcer.notification.subscription

import com.github.devcsrj.announcer.customer.CustomerId
import reactor.core.publisher.Mono

interface SubscriptionService {

    fun subscribe(customerId: CustomerId, request: SubscriptionRequest): Mono<Subscription>

    fun getSubscription(customerId: CustomerId): Mono<Subscription>
}