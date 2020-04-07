package com.github.devcsrj.announcer.notification.subscription

import com.github.devcsrj.announcer.customer.CustomerId
import reactor.core.publisher.Mono

interface SubscriptionRepository {

    fun save(subscription: Subscription): Mono<Subscription>

    fun findByCustomer(customerId: CustomerId): Mono<Subscription>
}