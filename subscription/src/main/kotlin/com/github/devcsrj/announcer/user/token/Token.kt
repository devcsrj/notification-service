package com.github.devcsrj.announcer.user.token

import org.springframework.data.annotation.Id
import java.util.*

data class Token(
    @Id
    val value: String,
    val name: String,
    val ownerId: OwnerId
) {

    companion object {
        fun generate(name: String, ownerId: OwnerId): Token {
            val token = UUID.randomUUID().toString()
            return Token(token, name, ownerId)
        }
    }
}