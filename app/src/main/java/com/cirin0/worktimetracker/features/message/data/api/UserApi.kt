package com.cirin0.worktimetracker.features.message.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.message.data.model.UsersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET(Constants.ApiRoutes.USERS)
    suspend fun getUsers(@Query("page") page: Int): UsersResponse
}

