package com.github.devcsrj.announcer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SubscriptionApplication

fun main(args: Array<String>) {
    runApplication<SubscriptionApplication>(*args)
}
