package com.github.devcsrj.announcer.notification.subscription

import com.github.devcsrj.announcer.customer.CustomerId
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(
    value = ["/api/subscriptions"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class SubscriptionController(private val subscriptionService: SubscriptionService) {

    @PostMapping(value = ["/{customerId}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun subscribe(
        @PathVariable customerId: String,
        @RequestBody request: SubscriptionRequest
    ): Mono<Subscription> {
        return subscriptionService.subscribe(CustomerId(customerId), request)
    }

    @GetMapping(value = ["/{customerId}"])
    fun findSubscriptionForRecipient(
        @PathVariable customerId: String
    ): Mono<ResponseEntity<Subscription>> {
        return subscriptionService.getSubscription(CustomerId(customerId))
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }
}