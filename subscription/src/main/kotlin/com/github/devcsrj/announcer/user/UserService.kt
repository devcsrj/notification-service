package com.github.devcsrj.announcer.user

import com.github.devcsrj.announcer.user.token.Token
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserService {

    fun createToken(userId: UserId, name: String): Mono<Token>

    fun getTokens(userId: UserId): Flux<Token>
}