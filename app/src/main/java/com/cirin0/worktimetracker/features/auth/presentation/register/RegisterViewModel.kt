package com.cirin0.worktimetracker.features.auth.presentation.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.utils.ValidationResult
import com.cirin0.worktimetracker.core.utils.ValidationRules
import com.cirin0.worktimetracker.features.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.update {
            val newState = it.copy(
                name = name,
                nameError = null,
                error = null,
                hasInteractedWithName = true
            )
            newState.copy(nameError = validateName(newState.name, newState.hasInteractedWithName))
        }
    }

    fun onEmailChange(email: String) {
        _state.update {
            val newState = it.copy(
                email = email,
                emailError = null,
                error = null,
                hasInteractedWithEmail = true
            )
            newState.copy(
                emailError = validateEmail(
                    newState.email,
                    newState.hasInteractedWithEmail
                )
            )
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
            newState.copy(
                passwordError = validatePassword(
                    newState.password,
                    newState.hasInteractedWithPassword
                )
            )
        }
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
                            userId = result.data.user.id,
                            successMessage = result.data.message
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
        val currentState = _state.value

        val nameError = validateName(currentState.name, true)
        val emailError = validateEmail(currentState.email, true)
        val passwordError = validatePassword(currentState.password, true)

        _state.update {
            it.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError
            )
        }

        return nameError == null && emailError == null && passwordError == null
    }

    private fun validateName(name: String, hasInteracted: Boolean): String? {
        if (!hasInteracted && name.isBlank()) return null
        return when (val result = ValidationRules.isValidName(name)) {
            is ValidationResult.Success -> null
            is ValidationResult.Error -> result.resolve(context)
        }
    }

    private fun validateEmail(email: String, hasInteracted: Boolean): String? {
        if (!hasInteracted && email.isBlank()) return null
        return when (val result = ValidationRules.isValidEmail(email)) {
            is ValidationResult.Success -> null
            is ValidationResult.Error -> result.resolve(context)
        }
    }

    private fun validatePassword(password: String, hasInteracted: Boolean): String? {
        if (!hasInteracted && password.isBlank()) return null
        return when (val result = ValidationRules.isValidPassword(password)) {
            is ValidationResult.Success -> null
            is ValidationResult.Error -> result.resolve(context)
        }
    }
}