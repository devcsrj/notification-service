package com.github.devcsrj.announcer.user.token

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface TokenRepository {

    fun findByOwner(ownerId: OwnerId): Flux<Token>
    fun save(token: Token): Mono<Token>
}