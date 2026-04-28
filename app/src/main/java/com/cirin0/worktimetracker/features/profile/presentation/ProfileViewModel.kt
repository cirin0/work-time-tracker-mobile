package com.cirin0.worktimetracker.features.profile.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.utils.ConnectivityObserver
import com.cirin0.worktimetracker.core.utils.ValidationResult
import com.cirin0.worktimetracker.core.utils.ValidationRules
import com.cirin0.worktimetracker.features.profile.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val connectivityObserver: ConnectivityObserver,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        loadUserProfile()
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                val isOffline = status != ConnectivityObserver.Status.Available
                _state.update { it.copy(isOffline = isOffline) }

                if (status == ConnectivityObserver.Status.Available && _state.value.isCachedData) {
                    loadUserProfile()
                }
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val isOffline = !connectivityObserver.isConnected()
            when (val response = profileRepository.getCurrentUser()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            user = response.data,
                            isLoading = false,
                            error = null,
                            editName = response.data.name,
                            isCachedData = response.fromCache,
                            showServerUnavailableWarning = response.fromCache && !isOffline
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
                nameError = null,
                updateError = null
            )
        }
    }

    fun closeEditDialog() {
        _state.update {
            it.copy(
                isEditDialogOpen = false,
                nameError = null,
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

    fun updateProfile() {
        val currentState = _state.value
        val userEmail = currentState.user?.email ?: ""

        val nameValidation = ValidationRules.isValidName(currentState.editName)

        if (!nameValidation.isValid) {
            _state.update {
                it.copy(
                    nameError = (nameValidation as? ValidationResult.Error)?.resolve(context)
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, updateError = null, updateSuccess = false) }
            when (val response = profileRepository.updateProfile(
                currentState.editName,
                userEmail
            )) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            user = response.data,
                            isUpdating = false,
                            updateSuccess = true,
                            isEditDialogOpen = false,
                            editName = response.data.name
                        )
                    }
                }

                is ApiResponse.Error -> {
                    val translatedError = translateError(response)
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateError = translatedError
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
                    val translatedError = translateError(response)
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateError = translatedError
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

    fun openPinCodeDialog() {
        _state.update {
            it.copy(
                isPinCodeDialogOpen = true,
                pinCode = "",
                pinCodeError = null,
                updateError = null
            )
        }
    }

    fun closePinCodeDialog() {
        _state.update {
            it.copy(
                isPinCodeDialogOpen = false,
                pinCode = "",
                pinCodeError = null
            )
        }
    }

    fun onPinCodeChange(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _state.update {
                it.copy(
                    pinCode = pin,
                    pinCodeError = null
                )
            }
        }
    }

    fun setupPinCode() {
        val pinCode = _state.value.pinCode
        if (pinCode.length != 4) {
            _state.update {
                it.copy(pinCodeError = context.getString(R.string.home_pin_must_be_four_digits))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, updateError = null, updateSuccess = false) }
            when (val response = profileRepository.setupPinCode(pinCode)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateSuccess = true,
                            isPinCodeDialogOpen = false,
                            pinCode = ""
                        )
                    }
                    loadUserProfile()
                }

                is ApiResponse.Error -> {
                    val translatedError = translateError(response)
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateError = translatedError
                        )
                    }
                }

                is ApiResponse.Loading -> {
                    _state.update { it.copy(isUpdating = true) }
                }
            }
        }
    }

    fun openUpdatePinCodeDialog() {
        _state.update {
            it.copy(
                isUpdatePinCodeDialogOpen = true,
                currentPinCode = "",
                newPinCode = "",
                currentPinCodeError = null,
                newPinCodeError = null,
                updateError = null
            )
        }
    }

    fun closeUpdatePinCodeDialog() {
        _state.update {
            it.copy(
                isUpdatePinCodeDialogOpen = false,
                currentPinCode = "",
                newPinCode = "",
                currentPinCodeError = null,
                newPinCodeError = null
            )
        }
    }

    fun onCurrentPinCodeChange(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _state.update {
                it.copy(
                    currentPinCode = pin,
                    currentPinCodeError = null
                )
            }
        }
    }

    fun onNewPinCodeChange(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _state.update {
                it.copy(
                    newPinCode = pin,
                    newPinCodeError = null
                )
            }
        }
    }

    fun updatePinCode() {
        val currentPin = _state.value.currentPinCode
        val newPin = _state.value.newPinCode

        val pinLengthError = context.getString(R.string.home_pin_must_be_four_digits)
        val currentError = if (currentPin.length != 4) pinLengthError else null
        val newError = if (newPin.length != 4) pinLengthError else null

        if (currentError != null || newError != null) {
            _state.update {
                it.copy(
                    currentPinCodeError = currentError,
                    newPinCodeError = newError
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, updateError = null, updateSuccess = false) }
            when (val response = profileRepository.updatePinCode(currentPin, newPin)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateSuccess = true,
                            isUpdatePinCodeDialogOpen = false,
                            currentPinCode = "",
                            newPinCode = ""
                        )
                    }
                    loadUserProfile()
                }

                is ApiResponse.Error -> {
                    val translatedError = translateError(response)
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            updateError = translatedError
                        )
                    }
                }

                is ApiResponse.Loading -> {
                    _state.update { it.copy(isUpdating = true) }
                }
            }
        }
    }

    private fun translateError(error: ApiResponse.Error): String {
        if (error.code == 422 && error.errors != null) {
            val allErrors = error.errors.values.flatten()
            if (allErrors.isNotEmpty()) {
                val firstError = allErrors.first()
                return when {
                    firstError.contains(
                        "pin code is required",
                        ignoreCase = true
                    ) -> context.getString(R.string.profile_pin_required)

                    firstError.contains(
                        "pin code must be exactly 4 digits",
                        ignoreCase = true
                    ) -> context.getString(R.string.profile_pin_exact_digits)

                    firstError.contains(
                        "pin code must contain only digits",
                        ignoreCase = true
                    ) -> context.getString(R.string.profile_pin_digits_only)

                    firstError.contains(
                        "must be different from the current one",
                        ignoreCase = true
                    ) -> context.getString(R.string.profile_pin_must_differ)

                    firstError.contains(
                        "current pin code is incorrect",
                        ignoreCase = true
                    ) -> context.getString(R.string.profile_pin_current_incorrect)

                    else -> firstError
                }
            }
        }

        return when {
            error.message.contains(
                "current pin code is incorrect",
                ignoreCase = true
            ) -> context.getString(R.string.profile_pin_current_incorrect)

            error.message.contains(
                "must be different from the current one",
                ignoreCase = true
            ) -> context.getString(R.string.profile_pin_must_differ)

            else -> error.message
        }
    }
}
