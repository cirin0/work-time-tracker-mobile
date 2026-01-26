package com.cirin0.worktimetracker.features.profile.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.profile.data.model.UpdateProfileRequest
import com.cirin0.worktimetracker.features.profile.data.model.UpdateProfileResponse
import com.cirin0.worktimetracker.features.profile.data.model.User
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface ProfileApi {
    @GET(Constants.ApiRoutes.ME)
    suspend fun getCurrentUser(): User

    @PATCH(Constants.ApiRoutes.UPDATE_PROFILE)
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UpdateProfileResponse

    @Multipart
    @POST(Constants.ApiRoutes.UPDATE_AVATAR)
    suspend fun updateAvatar(@Part avatar: MultipartBody.Part): User
}