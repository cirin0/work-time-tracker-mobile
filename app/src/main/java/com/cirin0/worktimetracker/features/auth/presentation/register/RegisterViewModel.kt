package com.cirin0.worktimetracker.features.auth.presentation.register

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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name, nameError = null, error = null) }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null, error = null) }
    }

    fun register() {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.register(
                _state.value.name,
                _state.value.email,
                _state.value.password
            )) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRegistered = true,
                            successMessage = result.data
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

    private fun validateInput(): Boolean {
        var isValid = true

        if (_state.value.name.isBlank()) {
            _state.update { it.copy(nameError = "Name is required") }
            isValid = false
        } else if (_state.value.name.length < 3) {
            _state.update { it.copy(nameError = "Name must be at least 3 characters") }
            isValid = false
        }

        if (_state.value.email.isBlank()) {
            _state.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()) {
            _state.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        if (_state.value.password.isBlank()) {
            _state.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (_state.value.password.length < 6) {
            _state.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        return isValid
    }
}