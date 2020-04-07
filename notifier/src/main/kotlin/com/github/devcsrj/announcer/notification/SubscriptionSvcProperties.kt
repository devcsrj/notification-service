package com.github.devcsrj.announcer.notification

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
@ConfigurationProperties(prefix = "announcer.subscription-svc")
class SubscriptionSvcProperties {

    lateinit var url: URI
}