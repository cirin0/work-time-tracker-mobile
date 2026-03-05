package com.cirin0.worktimetracker.features.auth.data.model

import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    @SerializedName("fcm_token")
    val fcmToken: String
)

