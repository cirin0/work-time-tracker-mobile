package com.cirin0.worktimetracker.features.auth.data.model

import com.google.gson.annotations.SerializedName

data class ResendVerificationResponse(
    @SerializedName("message") val message: String
)

