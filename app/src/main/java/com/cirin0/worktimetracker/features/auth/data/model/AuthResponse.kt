package com.cirin0.worktimetracker.features.auth.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: String,
    @SerializedName("user") val user: User
)

data class RegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User
)

data class RefreshResponse(
    @SerializedName("message") val message: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: String,
    @SerializedName("user") val user: User
)

data class LogoutResponse(
    @SerializedName("message") val message: String
)