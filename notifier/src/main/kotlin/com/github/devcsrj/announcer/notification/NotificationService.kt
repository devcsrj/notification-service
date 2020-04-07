package com.github.devcsrj.announcer.notification

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationService {

    @PUT("/api/notifications/{id}")
    fun update(@Path("id") notificationId: String, @Body status: UpdateNotificationRequest): Call<Void>
}