package com.github.devcsrj.announcer.user

import com.github.devcsrj.announcer.user.token.Token
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {

    @GET("/api/users/{userId}/tokens")
    fun getTokens(@Path("userId") userId: String): Call<List<Token>>
}