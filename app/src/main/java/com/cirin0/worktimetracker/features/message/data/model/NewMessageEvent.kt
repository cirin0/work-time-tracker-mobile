package com.cirin0.worktimetracker.features.message.data.model

import com.google.gson.annotations.SerializedName

data class NewMessageEvent(
    val id: Int? = null,
    val message: String,
    val user: MessageUser,
    @SerializedName("receiver_id") val receiverId: Int? = null,
    @SerializedName("sender_id") val senderId: Int? = null
)

data class MessageUser(
    val id: Int,
    val name: String,
    val email: String
)

