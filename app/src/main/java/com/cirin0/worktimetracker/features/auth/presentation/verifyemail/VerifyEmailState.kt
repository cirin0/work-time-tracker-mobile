package com.cirin0.worktimetracker.features.auth.presentation.verifyemail

data class VerifyEmailState(
    val code: String = "",
    val codeError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isVerified: Boolean = false,
    val hasInteracted: Boolean = false,
    val isResending: Boolean = false,
    val resendSuccess: String? = null,
    val isAutoLoginSuccess: Boolean = false
)
