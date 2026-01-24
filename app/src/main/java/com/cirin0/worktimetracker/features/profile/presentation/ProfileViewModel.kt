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
                            error = null
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
