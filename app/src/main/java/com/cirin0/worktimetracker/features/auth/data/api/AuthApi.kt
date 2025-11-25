package com.cirin0.worktimetracker.features.auth.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.auth.data.model.LoginRequest
import com.cirin0.worktimetracker.features.auth.data.model.LoginResponse
import com.cirin0.worktimetracker.features.auth.data.model.LogoutResponse
import com.cirin0.worktimetracker.features.auth.data.model.RefreshResponse
import com.cirin0.worktimetracker.features.auth.data.model.RegisterRequest
import com.cirin0.worktimetracker.features.auth.data.model.RegisterResponse
import com.cirin0.worktimetracker.features.auth.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST(Constants.ApiRoutes.LOGIN)
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST(Constants.ApiRoutes.REGISTER)
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST(Constants.ApiRoutes.REFRESH)
    suspend fun refreshToken(): RefreshResponse

    @POST(Constants.ApiRoutes.LOGOUT)
    suspend fun logout(): LogoutResponse

    @GET(Constants.ApiRoutes.ME)
    suspend fun getCurrentUser(): User
}