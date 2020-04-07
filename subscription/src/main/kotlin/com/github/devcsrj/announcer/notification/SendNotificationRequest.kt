package com.github.devcsrj.announcer.notification

data class SendNotificationRequest(
    val customerId: String,
    val type: String,
    val payload: Map<String, Any>
)