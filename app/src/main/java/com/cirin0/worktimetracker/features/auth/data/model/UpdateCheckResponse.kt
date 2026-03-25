package com.cirin0.worktimetracker.features.auth.data.model

data class UpdateCheckResponse(
    val updateAvailable: Boolean,
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val changelog: String?
)

