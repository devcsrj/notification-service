package com.github.devcsrj.announcer.user

import com.github.devcsrj.announcer.user.token.MongoTokenRepository
import com.github.devcsrj.announcer.user.token.TokenRepository
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.stereotype.Component

@Component
@EnableReactiveMongoRepositories(considerNestedRepositories = true)
class UserModule {

    @Bean
    fun tokenRepository(dao: MongoTokenRepository.Dao): TokenRepository {
        return MongoTokenRepository(dao)
    }

    @Bean
    fun userService(repository: TokenRepository): UserService {
        return DefaultUserService(repository)
    }
}