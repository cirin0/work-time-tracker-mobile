package com.cirin0.worktimetracker.features.profile.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.profile.data.model.User
import retrofit2.http.GET

interface ProfileApi {
    @GET(Constants.ApiRoutes.ME)
    suspend fun getCurrentUser(): User
}