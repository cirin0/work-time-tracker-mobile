package com.cirin0.worktimetracker.features.timeentries.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.utils.GpsLocationResult
import com.cirin0.worktimetracker.core.utils.LocationManager
import com.cirin0.worktimetracker.features.profile.data.repository.ProfileRepository
import com.cirin0.worktimetracker.features.timeentries.data.repository.TimeEntriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TimeEntriesViewModel @Inject constructor(
    private val repository: TimeEntriesRepository,
    private val locationManager: LocationManager,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TimeEntriesState())
    val state: StateFlow<TimeEntriesState> = _state.asStateFlow()

    init {
        loadUserData()
        loadActiveEntry()
        loadTimeEntries()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            when (val result = profileRepository.getCurrentUser()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(user = result.data)
                }

                is ApiResponse.Error -> {
                    // User data failed to load, but we can still show the screen
                }

                is ApiResponse.Loading -> {}
            }
        }
    }

    fun loadActiveEntry() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = repository.getActiveTimeEntry()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        activeEntry = result.data,
                        isLoading = false
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                is ApiResponse.Loading -> {}
            }
        }
    }

    fun startTimeEntry() {
        viewModelScope.launch {
            _state.value = _state.value.copy(qrCodeScanSuccess = false)

            val user = _state.value.user

            val needsGPS = when {
                user == null -> true // Default to requiring GPS if user data not loaded
                user.isRemote() -> false // Remote workers don't need GPS
                user.requiresGPS() -> true // Office workers always need GPS
                user.isHybrid() -> _state.value.isInOffice // Hybrid - depends on checkbox
                else -> true
            }

            if (needsGPS) {
                _state.value = _state.value.copy(isLoadingLocation = true, error = null)

                when (val locationResult = locationManager.getCurrentLocation()) {
                    is GpsLocationResult.Success -> {
                        _state.value = _state.value.copy(
                            isLoadingLocation = false,
                            currentLatitude = locationResult.location.latitude,
                            currentLongitude = locationResult.location.longitude,
                            locationPermissionDenied = false,
                            isLoading = true
                        )

                        val comment = _state.value.startComment.takeIf { it.isNotBlank() }
                        val qrCode = _state.value.qrCodeScanned
                        when (val result = repository.startTimeEntry(
                            comment,
                            locationResult.location.latitude,
                            locationResult.location.longitude,
                            qrCode
                        )) {
                            is ApiResponse.Success -> {
                                _state.value = _state.value.copy(
                                    activeEntry = result.data,
                                    isLoading = false,
                                    startComment = "",
                                    qrCodeScanned = null
                                )
                                loadTimeEntries()
                            }

                            is ApiResponse.Error -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    qrCodeScanSuccess = false,
                                    error = result.message
                                )
                            }

                            is ApiResponse.Loading -> {}
                        }
                    }

                    is GpsLocationResult.PermissionDenied -> {
                        _state.value = _state.value.copy(
                            isLoadingLocation = false,
                            isLoading = false,
                            locationPermissionDenied = true,
                            qrCodeScanSuccess = false,
                            error = "Location permission is required to start work"
                        )
                    }

                    is GpsLocationResult.Error -> {
                        _state.value = _state.value.copy(
                            isLoadingLocation = false,
                            isLoading = false,
                            qrCodeScanSuccess = false,
                            error = "Failed to get location: ${locationResult.message}"
                        )
                    }
                }
            } else {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val comment = _state.value.startComment.takeIf { it.isNotBlank() }
                val qrCode = _state.value.qrCodeScanned

                when (val result = repository.startTimeEntry(comment, null, null, qrCode)) {
                    is ApiResponse.Success -> {
                        _state.value = _state.value.copy(
                            activeEntry = result.data,
                            isLoading = false,
                            startComment = "",
                            qrCodeScanned = null
                        )
                        loadTimeEntries()
                    }

                    is ApiResponse.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            qrCodeScanSuccess = false,
                            error = result.message
                        )
                    }

                    is ApiResponse.Loading -> {}
                }
            }
        }
    }

    fun stopTimeEntry() {
        viewModelScope.launch {
            if (_state.value.pinCode.length != 4) {
                _state.value = _state.value.copy(
                    error = "PIN-код має містити 4 цифри"
                )
                return@launch
            }

            if (!_state.value.pinCode.all { it.isDigit() }) {
                _state.value = _state.value.copy(
                    error = "PIN-код має містити тільки цифри"
                )
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)
            val comment = _state.value.stopComment.takeIf { it.isNotBlank() }
            val pinCode = _state.value.pinCode

            when (val result = repository.stopTimeEntry(comment, pinCode)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        activeEntry = null,
                        isLoading = false,
                        stopComment = "",
                        pinCode = ""
                    )
                    loadTimeEntries()
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                is ApiResponse.Loading -> {}
            }
        }
    }

    fun updateStartComment(comment: String) {
        _state.value = _state.value.copy(startComment = comment)
    }

    fun updateStopComment(comment: String) {
        _state.value = _state.value.copy(stopComment = comment)
    }

    fun updatePinCode(pinCode: String) {
        if (pinCode.all { it.isDigit() } && pinCode.length <= 4) {
            _state.value = _state.value.copy(pinCode = pinCode, error = null)
        }
    }

    fun toggleIsInOffice(isInOffice: Boolean) {
        _state.value = _state.value.copy(isInOffice = isInOffice)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            _state.value = _state.value.copy(locationPermissionDenied = false)
        } else {
            _state.value = _state.value.copy(
                locationPermissionDenied = true,
                error = "Location permission is required to track work time"
            )
        }
    }

    fun showQRScanner() {
        _state.value = _state.value.copy(showQRScanner = true, error = null)
    }

    fun hideQRScanner() {
        _state.value = _state.value.copy(
            showQRScanner = false,
            qrCodeScanned = null,
            qrCodeScanSuccess = false
        )
    }

    fun onQRCodeScanned(qrCode: String) {
        _state.value = _state.value.copy(
            qrCodeScanned = qrCode,
            showQRScanner = false,
            qrCodeScanSuccess = true
        )
        // Don't auto-start here - let the UI handle GPS permission check
    }

    fun onCameraPermissionResult(granted: Boolean) {
        if (granted) {
            _state.value = _state.value.copy(cameraPermissionDenied = false)
        } else {
            _state.value = _state.value.copy(
                cameraPermissionDenied = true,
                showQRScanner = false,
                error = "Для сканування QR-коду потрібен доступ до камери"
            )
        }
    }

    fun loadTimeEntries() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingList = true, listError = null)
            when (val result = repository.getTimeEntries()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        timeEntries = result.data,
                        isLoadingList = false
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoadingList = false,
                        listError = result.message
                    )
                }

                is ApiResponse.Loading -> {}
            }
        }
    }
}