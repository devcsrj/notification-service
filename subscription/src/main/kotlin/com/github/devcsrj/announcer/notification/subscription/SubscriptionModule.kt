package com.github.devcsrj.announcer.notification.subscription

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class SubscriptionModule {

    @Bean
    internal fun subscriptionRepository(dao: MongoSubscriptionRepository.Dao): SubscriptionRepository {
        return MongoSubscriptionRepository(dao)
    }

    @Bean
    internal fun subscriptionService(subscriptionRepository: SubscriptionRepository): SubscriptionService {
        return DefaultSubscriptionService(subscriptionRepository)
    }
}