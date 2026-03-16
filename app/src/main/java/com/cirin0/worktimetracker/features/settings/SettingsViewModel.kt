package com.cirin0.worktimetracker.features.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.localization.AppLocaleManager
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.utils.LocationManager
import com.cirin0.worktimetracker.features.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val hasCameraPermission: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val appLanguage: String = AppLocaleManager.DEFAULT_LANGUAGE
)

data class LogoutState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val locationManager: LocationManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _logoutState = MutableStateFlow(LogoutState())
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    init {
        _state.update { it.copy(appLanguage = AppLocaleManager.getCurrentLanguage()) }
        checkPermissions()
    }

    fun checkPermissions() {
        _state.update {
            it.copy(
                hasCameraPermission = hasCameraPermission(),
                hasLocationPermission = locationManager.hasLocationPermission()
            )
        }
    }

    fun setAppLanguage(language: String) {
        val normalizedLanguage = AppLocaleManager.normalizeLanguage(language)
        _state.update { it.copy(appLanguage = normalizedLanguage) }
        AppLocaleManager.applyAppLanguage(normalizedLanguage)
    }

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
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


