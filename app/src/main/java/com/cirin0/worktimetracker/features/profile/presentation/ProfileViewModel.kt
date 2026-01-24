package com.cirin0.worktimetracker.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.auth.data.repository.AuthRepository
import com.cirin0.worktimetracker.features.profile.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _logoutState = MutableStateFlow(LogoutState())
    val logoutState = _logoutState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val response = profileRepository.getCurrentUser()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            user = response.data,
                            isLoading = false,
                            error = null,
                            editName = response.data.name,
                            editEmail = response.data.email
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }
                }

                is ApiResponse.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun openEditDialog() {
        _state.update {
            it.copy(
                isEditDialogOpen = true,
                editName = it.user?.name ?: "",
                editEmail = it.user?.email ?: "",
                nameError = null,
                emailError = null,
                updateError = null
            )
        }
    }

    fun closeEditDialog() {
        _state.update {
            it.copy(
                isEditDialogOpen = false,
                nameError = null,
                emailError = null,
                updateError = null
            )
        }
    }

    fun onNameChange(name: String) {
        _state.update {
            it.copy(
                editName = name,
                nameError = null,
                updateError = null
            )
        }
    }

    fun onEmailChange(email: String) {
        _state.update {
            it.copy(
                editEmail = email,
                emailError = null,
                updateError = null
            )
        }
    }

    fun updateProfile() {
        val currentState = _state.value

        val nameError = if (currentState.editName.isBlank()) "Ім'я не може бути порожнім" else null
        val emailError = if (currentState.editEmail.isBlank()) {
            "Email не може бути порожнім"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.editEmail).matches()) {
            "Невірний формат email"
        } else null

        if (nameError != null || emailError != null) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, updateError = null, updateSuccess = false) }
            when (val response = profileRepository.updateProfile(
                currentState.editName,
                currentState.editEmail
            )) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            user = response.data,
                            isUpdating = false,
                            updateSuccess = true,
                            isEditDialogOpen = false,
                            editName = response.data.name,
                            editEmail = response.data.email
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateError = response.message
                        )
                    }
                }

                is ApiResponse.Loading -> {
                    _state.update { it.copy(isUpdating = true) }
                }
            }
        }
    }

    fun updateAvatar(imageFile: File) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, updateError = null, updateSuccess = false) }
            when (val response = profileRepository.updateAvatar(imageFile)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            user = response.data,
                            isUpdating = false,
                            updateSuccess = true
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateError = response.message
                        )
                    }
                }

                is ApiResponse.Loading -> {
                    _state.update { it.copy(isUpdating = true) }
                }
            }
            loadUserProfile()
        }
    }

    fun clearUpdateSuccess() {
        _state.update { it.copy(updateSuccess = false) }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.update { it.copy(isLoading = true, error = null) }
            when (val response = authRepository.logout()) {
                is ApiResponse.Success -> {
                    _logoutState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            error = null
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _logoutState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }
                }

                is ApiResponse.Loading -> {
                    _logoutState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}

data class LogoutState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
