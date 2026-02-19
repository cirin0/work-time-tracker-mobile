package com.cirin0.worktimetracker.features.profile.presentation

import com.cirin0.worktimetracker.features.profile.data.model.User

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val updateError: String? = null,
    val updateSuccess: Boolean = false,
    val isEditDialogOpen: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val isOffline: Boolean = false,
    val isCachedData: Boolean = false,
    val showServerUnavailableWarning: Boolean = false,
    val isPinCodeDialogOpen: Boolean = false,
    val isUpdatePinCodeDialogOpen: Boolean = false,
    val pinCode: String = "",
    val currentPinCode: String = "",
    val newPinCode: String = "",
    val pinCodeError: String? = null,
    val currentPinCodeError: String? = null,
    val newPinCodeError: String? = null
)
