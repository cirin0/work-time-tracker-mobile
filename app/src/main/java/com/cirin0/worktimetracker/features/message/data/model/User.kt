package com.cirin0.worktimetracker.features.message.data.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String? = null
)
