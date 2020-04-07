package com.github.devcsrj.announcer.notification.subscription

import com.github.devcsrj.announcer.customer.CustomerId
import reactor.core.publisher.Mono
import java.time.LocalDateTime

internal class DefaultSubscriptionService(
    private val repo: SubscriptionRepository
) : SubscriptionService {


    override fun subscribe(customerId: CustomerId, request: SubscriptionRequest): Mono<Subscription> {
        return repo.findByCustomer(customerId)
            .switchIfEmpty(
                Mono.just(request).map {
                    Subscription(
                        id = SubscriptionId.create(),
                        customerId = customerId,
                        date = LocalDateTime.now(),
                        callbackUrl = request.callbackUrl
                    )
                })
            .map { it.copy(callbackUrl = request.callbackUrl) }
            .flatMap { repo.save(it) }
    }

    override fun getSubscription(customerId: CustomerId): Mono<Subscription> {
        return repo.findByCustomer(customerId)
    }
}