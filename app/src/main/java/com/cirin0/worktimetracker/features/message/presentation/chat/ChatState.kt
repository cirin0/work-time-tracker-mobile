package com.cirin0.worktimetracker.features.message.presentation.chat

import com.cirin0.worktimetracker.features.message.data.model.Message

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val messageText: String = "",
    val isSending: Boolean = false,
    val currentUserId: Int = 0,
    val receiverId: Int = 0
)

