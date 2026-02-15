package com.cirin0.worktimetracker.features.message.data.model

import com.google.gson.annotations.SerializedName

data class SendMessageRequest(
    val message: String,
    @SerializedName("receiver_id") val receiverId: Int,
)