package com.github.devcsrj.announcer.notification.subscription

import com.github.devcsrj.announcer.customer.CustomerId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

class MongoSubscriptionRepository(private val dao: Dao) : SubscriptionRepository {

    override fun save(subscription: Subscription): Mono<Subscription> = dao.save(subscription)
    override fun findByCustomer(customerId: CustomerId) = dao.findByCustomerId(customerId)

    @Repository("mongoSubscriptionDao")
    interface Dao : ReactiveMongoRepository<Subscription, SubscriptionId> {
        fun findByCustomerId(customerId: CustomerId): Mono<Subscription>
    }
}