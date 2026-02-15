package com.cirin0.worktimetracker.features.message.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.message.data.model.Message
import com.cirin0.worktimetracker.features.message.data.model.SendMessageRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {
    @GET(Constants.ApiRoutes.MESSAGES_BY_RECEIVER)
    suspend fun getMessages(@Path("receiverId") receiverId: Int): List<Message>

    @POST(Constants.ApiRoutes.MESSAGES)
    suspend fun sendMessage(@Body body: SendMessageRequest): Message
}
