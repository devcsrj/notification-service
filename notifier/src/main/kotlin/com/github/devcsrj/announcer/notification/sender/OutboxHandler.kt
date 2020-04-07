package com.github.devcsrj.announcer.notification.sender

import com.github.devcsrj.announcer.notification.Notification

interface OutboxHandler {

    fun handle(notification: Notification)
}