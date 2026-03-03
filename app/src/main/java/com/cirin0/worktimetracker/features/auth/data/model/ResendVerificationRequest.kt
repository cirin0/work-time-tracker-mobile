package com.cirin0.worktimetracker.features.auth.data.model

import com.google.gson.annotations.SerializedName

data class ResendVerificationRequest(
    @SerializedName("email") val email: String
)

