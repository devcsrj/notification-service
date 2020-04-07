package com.github.devcsrj.announcer.notification.subscription

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SubscriptionService {

    @GET("/api/subscriptions/{recipient}")
    fun getSubscription(@Path("recipient") recipient: String): Call<Subscription>
}