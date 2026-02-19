package com.cirin0.worktimetracker.features.message.data.model

import com.google.gson.annotations.SerializedName

data class Message(
    val id: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("receiver_id") val receiverId: Int,
    val message: String,
    @SerializedName("created_at") val createdAt: String,
)