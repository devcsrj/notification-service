package com.github.devcsrj.announcer.user

import com.github.devcsrj.announcer.user.token.Token
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping(
    value = ["/api/users"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class UserController(private val userService: UserService) {

    @PostMapping(value = ["/{userId}/tokens"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun generateUserToken(
        @PathVariable userId: String,
        @RequestBody body: GenerateTokenRequest
    ): Mono<Token> {
        return userService.createToken(UserId(userId), body.name)
    }

    @GetMapping(value = ["/{userId}/tokens"])
    fun getUserTokens(@PathVariable userId: String): Flux<Token> {
        return userService.getTokens(UserId(userId))
    }
}