package com.cirin0.worktimetracker.features.profile.data.model

import com.google.gson.annotations.SerializedName

data class UpdatePinCodeRequest(
    @SerializedName("current_pin_code") val currentPinCode: String,
    @SerializedName("new_pin_code") val newPinCode: String
)
