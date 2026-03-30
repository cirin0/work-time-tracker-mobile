package com.cirin0.worktimetracker.features.message.presentation.chatlist

import com.cirin0.worktimetracker.features.message.data.model.User

data class ChatListState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
    val error: String? = null,
    val loadMoreError: String? = null
)

