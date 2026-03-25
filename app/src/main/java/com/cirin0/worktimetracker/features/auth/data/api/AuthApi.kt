package com.cirin0.worktimetracker.features.auth.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.auth.data.model.FcmTokenRequest
import com.cirin0.worktimetracker.features.auth.data.model.FcmTokenResponse
import com.cirin0.worktimetracker.features.auth.data.model.LoginRequest
import com.cirin0.worktimetracker.features.auth.data.model.LoginResponse
import com.cirin0.worktimetracker.features.auth.data.model.LogoutResponse
import com.cirin0.worktimetracker.features.auth.data.model.RefreshResponse
import com.cirin0.worktimetracker.features.auth.data.model.RegisterRequest
import com.cirin0.worktimetracker.features.auth.data.model.RegisterResponse
import com.cirin0.worktimetracker.features.auth.data.model.ResendVerificationRequest
import com.cirin0.worktimetracker.features.auth.data.model.ResendVerificationResponse
import com.cirin0.worktimetracker.features.auth.data.model.UpdateCheckResponse
import com.cirin0.worktimetracker.features.auth.data.model.VerifyEmailRequest
import com.cirin0.worktimetracker.features.auth.data.model.VerifyEmailResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST(Constants.ApiRoutes.LOGIN)
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST(Constants.ApiRoutes.REGISTER)
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST(Constants.ApiRoutes.VERIFY_EMAIL)
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): VerifyEmailResponse

    @POST(Constants.ApiRoutes.RESEND_VERIFICATION_CODE)
    suspend fun resendVerificationCode(@Body request: ResendVerificationRequest): ResendVerificationResponse

    @POST(Constants.ApiRoutes.REFRESH)
    suspend fun refreshToken(): RefreshResponse

    @POST(Constants.ApiRoutes.LOGOUT)
    suspend fun logout(): LogoutResponse

    @PATCH(Constants.ApiRoutes.FCM_TOKEN)
    suspend fun sendFcmToken(@Body request: FcmTokenRequest): FcmTokenResponse

    @GET("app/update-check")
    suspend fun checkForUpdates(
        @Query("version_code") versionCode: Int,
        @Query("platform") platform: String = "android",
        @Query("channel") channel: String = "stable"
    ): UpdateCheckResponse
}