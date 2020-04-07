package com.github.devcsrj.announcer.user

import com.github.devcsrj.announcer.user.token.OwnerId
import com.github.devcsrj.announcer.user.token.Token
import com.github.devcsrj.announcer.user.token.TokenRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * For brevity, this implementation assumes that any user is valid
 */
class DefaultUserService(private val tokenRepo: TokenRepository) : UserService {

    override fun createToken(userId: UserId, name: String): Mono<Token> {
        return Mono
            .just(Token.generate(name, OwnerId(userId.value)))
            .flatMap { tokenRepo.save(it) }
    }

    override fun getTokens(userId: UserId): Flux<Token> {
        return tokenRepo.findByOwner(OwnerId(userId.value))
    }
}