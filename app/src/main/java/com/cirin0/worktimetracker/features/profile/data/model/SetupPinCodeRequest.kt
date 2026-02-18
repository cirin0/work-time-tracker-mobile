package com.cirin0.worktimetracker.features.profile.data.model

import com.google.gson.annotations.SerializedName

data class SetupPinCodeRequest(
    @SerializedName("pin_code") val pinCode: String
)
