package com.cirin0.worktimetracker.features.message.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.message.data.model.UsersResponse
import retrofit2.http.GET

interface UserApi {
    @GET(Constants.ApiRoutes.USERS)
    suspend fun getUsers(): UsersResponse
}

