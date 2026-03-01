package com.cirin0.worktimetracker.features.auth.presentation.verifyemail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val email: String = savedStateHandle.get<String>("email") ?: ""

    private val _state = MutableStateFlow(VerifyEmailState())
    val state = _state.asStateFlow()

    fun onCodeChange(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _state.update {
                val newState = it.copy(
                    code = code,
                    codeError = null,
                    error = null,
                    hasInteracted = true
                )
                newState.copy(codeError = validateCode(newState.code, newState.hasInteracted))
            }
        }
    }

    fun verifyEmail() {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.verifyEmail(email, _state.value.code)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isVerified = true
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                is ApiResponse.Loading -> {}
            }
        }
    }

    fun resendCode() {
        viewModelScope.launch {
            _state.update { it.copy(isResending = true, error = null, resendSuccess = null) }

            when (val result = authRepository.resendVerificationCode(email)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            isResending = false,
                            resendSuccess = result.data
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isResending = false,
                            error = result.message
                        )
                    }
                }

                is ApiResponse.Loading -> {}
            }
        }
    }

    private fun validateInput(): Boolean {
        val codeError = validateCode(_state.value.code, true)

        _state.update {
            it.copy(codeError = codeError)
        }

        return codeError == null
    }

    private fun validateCode(code: String, hasInteracted: Boolean): String? {
        if (!hasInteracted && code.isBlank()) return null
        if (code.isBlank()) return "Verification code is required"
        if (code.length != 6) return "Code must be 6 digits"
        return null
    }
}

