package com.github.devcsrj.announcer.notification

import com.github.devcsrj.announcer.notification.outbox.CompositeOutbox
import com.github.devcsrj.announcer.notification.outbox.Outbox
import com.github.devcsrj.announcer.notification.outbox.PersistingOutbox
import com.github.devcsrj.announcer.notification.outbox.StreamedOutbox
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.stereotype.Component

@Component
@EnableReactiveMongoRepositories(considerNestedRepositories = true)
class NotificationModule {

    @Bean
    fun notificationRepository(dao: MongoNotificationRepository.Dao): NotificationRepository {
        return MongoNotificationRepository(dao)
    }

    @Bean
    fun persistingOutbox(notificationRepository: NotificationRepository): PersistingOutbox {
        return PersistingOutbox(notificationRepository)
    }

    @Primary
    @Bean
    fun outbox(
        streamedOutbox: StreamedOutbox,
        persistingOutbox: PersistingOutbox
    ): Outbox {
        return CompositeOutbox( // order is important
            listOf(persistingOutbox, streamedOutbox)
        )
    }
}