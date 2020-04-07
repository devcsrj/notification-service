package com.github.devcsrj.announcer.notification

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.devcsrj.announcer.notification.sender.NotificationCallbackHandler
import com.github.devcsrj.announcer.notification.sender.OutboxHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.context.annotation.Bean

@EnableBinding(Sink::class)
@SpringBootApplication
class NotifierApplication {

    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.registerModule(KotlinModule())
        mapper.registerModule(JavaTimeModule())
        return mapper
    }

    @Bean
    fun outboxHandler(
        properties: SubscriptionSvcProperties,
        objectMapper: ObjectMapper
    ): OutboxHandler {
        return NotificationCallbackHandler(properties.url, objectMapper)
    }
}

fun main(args: Array<String>) {
    runApplication<NotifierApplication>(*args)
}
