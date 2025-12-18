package com.cirin0.worktimetracker.features.auth.presentation.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { 
            val newState = it.copy(
                email = email, 
                emailError = null, 
                error = null,
                hasInteractedWithEmail = true
            )
            newState.copy(emailError = validateEmail(newState.email, newState.hasInteractedWithEmail))
        }
    }

    fun onPasswordChange(password: String) {
        _state.update { 
            val newState = it.copy(
                password = password, 
                passwordError = null, 
                error = null,
                hasInteractedWithPassword = true
            )
            newState.copy(passwordError = validatePassword(newState.password, newState.hasInteractedWithPassword))
        }
    }

    fun login() {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.login(
                email = _state.value.email,
                password = _state.value.password
            )) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true
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

                is ApiResponse.Loading -> {
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val currentState = _state.value
        var isValid = true

        val emailError = validateEmail(currentState.email, true)
        val passwordError = validatePassword(currentState.password, true)

        _state.update { 
            it.copy(
                emailError = emailError,
                passwordError = passwordError
            )
        }

        isValid = emailError == null && passwordError == null
        return isValid
    }

    private fun validateEmail(email: String, hasInteracted: Boolean): String? {
        if (!hasInteracted && email.isBlank()) return null
        if (email.isBlank()) return "Email is required"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Invalid email format"
        return null
    }

    private fun validatePassword(password: String, hasInteracted: Boolean): String? {
        if (!hasInteracted && password.isBlank()) return null
        if (password.isBlank()) return "Password is required"
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.update { it.copy(isLoggedIn = false) }
        }
    }
}