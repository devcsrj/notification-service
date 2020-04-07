package com.github.devcsrj.announcer.user.token

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class MongoTokenRepository(private val dao: Dao) : TokenRepository {

    override fun findByOwner(ownerId: OwnerId): Flux<Token> {
        return dao.findByOwnerId(ownerId)
    }

    override fun save(token: Token): Mono<Token> {
        return dao.save(token)
    }

    interface Dao : ReactiveMongoRepository<Token, String> {
        fun findByOwnerId(ownerId: OwnerId): Flux<Token>
    }
}