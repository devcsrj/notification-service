package com.github.devcsrj.announcer.notification.subscription

import com.github.devcsrj.announcer.customer.CustomerId
import reactor.core.publisher.Mono

internal class InMemorySubscriptionRepository : SubscriptionRepository {

    private val map = mutableMapOf<CustomerId, Subscription>()

    override fun save(subscription: Subscription): Mono<Subscription> {
        return Mono.create {
            map[subscription.customerId] = subscription
            it.success(subscription)
        }
    }

    override fun findByCustomer(customerId: CustomerId): Mono<Subscription> {
        return Mono.justOrEmpty(map[customerId])
    }
}