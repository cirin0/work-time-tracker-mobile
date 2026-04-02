package com.cirin0.worktimetracker.features.timeentries.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.utils.DateUtils
import com.cirin0.worktimetracker.core.utils.GpsLocationResult
import com.cirin0.worktimetracker.core.utils.LocationManager
import com.cirin0.worktimetracker.features.profile.data.repository.ProfileRepository
import com.cirin0.worktimetracker.features.timeentries.data.repository.TimeEntriesRepository
import com.cirin0.worktimetracker.features.workschedule.data.repository.WorkScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel
class TimeEntriesViewModel @Inject constructor(
    private val repository: TimeEntriesRepository,
    private val locationManager: LocationManager,
    private val profileRepository: ProfileRepository,
    private val workScheduleRepository: WorkScheduleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TimeEntriesState())
    val state: StateFlow<TimeEntriesState> = _state.asStateFlow()

    private val _tickerFlow = flow {
        while (true) {
            emit(Unit)
            delay(60_000)
        }
    }

    val workProgressState: StateFlow<WorkProgressUiState> =
        combine(_state, _tickerFlow) { s, _ -> computeWorkProgressUiState(s) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, WorkProgressUiState())

    private fun computeWorkProgressUiState(s: TimeEntriesState): WorkProgressUiState {
        val activeEntry = s.activeEntry
        val todayDate = DateUtils.toIsoDate(System.currentTimeMillis())
        val todayEntries = s.timeEntries.filter { it.date == todayDate }

        val workedMinutes = if (activeEntry != null) {
            val currentTime = LocalTime.now()
            val startTime = try {
                if (activeEntry.startTime.contains("T")) {
                    ZonedDateTime.parse(activeEntry.startTime)
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalTime()
                } else {
                    LocalTime.parse(activeEntry.startTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                }
            } catch (_: Exception) {
                LocalTime.now()
            }
            val activeMinutes = Duration.between(startTime, currentTime)
                .toMinutes().toInt().coerceAtLeast(0)
            activeMinutes + todayEntries
                .filter { it.id != activeEntry.id }
                .sumOf { (it.duration ?: 0) / 60 }
        } else {
            todayEntries.sumOf { (it.duration ?: 0) / 60 }
        }

        return WorkProgressUiState(
            workedMinutes = workedMinutes,
            targetHours = s.targetHours,
            isWorkingDay = s.isWorkingDay,
            isTracking = activeEntry != null,
        )
    }

    init {
        loadUserData()
        loadActiveEntry()
        loadTimeEntries()
        loadWorkSchedule()
    }

    fun loadUserData() {
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

    fun loadWorkSchedule() {
        viewModelScope.launch {
            when (val result = workScheduleRepository.getMyWorkSchedule()) {
                is ApiResponse.Success -> {
                    val schedule = result.data
                    val currentDay = DateUtils.getCurrentDayOfWeek()
                    val todaySchedule =
                        schedule?.dailySchedules?.find { it.dayOfWeek.lowercase() == currentDay }

                    val isWorkingDay = todaySchedule?.isWorkingDay ?: true
                    val targetHours = if (todaySchedule != null && isWorkingDay) {
                        try {
                            val start = LocalTime.parse(todaySchedule.startTime)
                            val end = LocalTime.parse(todaySchedule.endTime)
                            val durationMinutes = Duration.between(start, end).toMinutes()
                            (durationMinutes - todaySchedule.breakDuration) / 60f
                        } catch (_: Exception) {
                            8f
                        }
                    } else {
                        8f
                    }

                    _state.value = _state.value.copy(
                        workSchedule = schedule,
                        targetHours = targetHours,
                        isWorkingDay = isWorkingDay
                    )
                }

                is ApiResponse.Error -> {}
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
                        uiError = null,
                        error = result.message
                    )
                }

                is ApiResponse.Loading -> {}
            }
        }
    }

    fun startTimeEntry() {
        viewModelScope.launch {
            // Check if user has PIN code set up
            val user = _state.value.user
            if (user?.hasPinCode == false) {
                _state.value = _state.value.copy(
                    showSetupPinDialog = true,
                    error = null
                )
                return@launch
            }

            _state.value = _state.value.copy(qrCodeScanSuccess = false)

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
                                    uiError = null,
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
                            uiError = null,
                            error = null
                        )
                    }

                    is GpsLocationResult.Error -> {
                        _state.value = _state.value.copy(
                            isLoadingLocation = false,
                            isLoading = false,
                            qrCodeScanSuccess = false,
                            uiError = null,
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
                            uiError = null,
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
                    uiError = TimeEntriesUiError.PIN_LENGTH,
                    error = null
                )
                return@launch
            }

            if (!_state.value.pinCode.all { it.isDigit() }) {
                _state.value = _state.value.copy(
                    uiError = TimeEntriesUiError.PIN_DIGITS_ONLY,
                    error = null
                )
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, uiError = null, error = null)
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
                        uiError = null,
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
            _state.value = _state.value.copy(
                pinCode = pinCode,
                uiError = null,
                error = if (_state.value.error?.contains(
                        "Invalid pin code",
                        ignoreCase = true
                    ) == true
                ) null else _state.value.error
            )
        }
    }

    fun toggleIsInOffice(isInOffice: Boolean) {
        _state.value = _state.value.copy(isInOffice = isInOffice)
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
                uiError = null,
                error = null
            )
        }
    }

    fun showQRScanner() {
        _state.value = _state.value.copy(showQRScanner = true, uiError = null, error = null)
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
                uiError = null,
                error = null
            )
        }
    }

    fun loadTimeEntries() {
        viewModelScope.launch {
            _state.value =
                _state.value.copy(isLoadingList = true, listError = null, currentPage = 1)
            when (val result = repository.getTimeEntries(page = 1)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        timeEntries = result.data.data,
                        isLoadingList = false,
                        showServerUnavailableWarning = false,
                        hasMore = (result.data.meta?.currentPage ?: 1) < (result.data.meta?.lastPage
                            ?: 1)
                    )
                }

                is ApiResponse.Error -> {
                    val cachedEntries = repository.getCachedTimeEntries()
                    _state.value = _state.value.copy(
                        timeEntries = cachedEntries,
                        isLoadingList = false,
                        listError = if (cachedEntries.isEmpty()) result.message else null,
                        showServerUnavailableWarning = cachedEntries.isNotEmpty()
                    )
                }

                is ApiResponse.Loading -> {}
            }
        }
    }

    fun loadMoreTimeEntries() {
        if (_state.value.isLoadingMore || !_state.value.hasMore) return

        viewModelScope.launch {
            val nextPage = _state.value.currentPage + 1
            _state.value = _state.value.copy(isLoadingMore = true)

            when (val result = repository.getTimeEntries(page = nextPage)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        timeEntries = _state.value.timeEntries + result.data.data,
                        isLoadingMore = false,
                        currentPage = nextPage,
                        hasMore = (result.data.meta?.currentPage ?: 1) < (result.data.meta?.lastPage
                            ?: 1)
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoadingMore = false,
                        listError = result.message
                    )
                }

                is ApiResponse.Loading -> {}
            }
        }
    }

    fun updateSetupPinCode(pin: String) {
        if (pin.all { it.isDigit() } && pin.length <= 4) {
            _state.value = _state.value.copy(setupPinCode = pin)
        }
    }

    fun updateSetupPinConfirm(pin: String) {
        if (pin.all { it.isDigit() } && pin.length <= 4) {
            _state.value = _state.value.copy(setupPinConfirm = pin)
        }
    }

    fun setupPinCode() {
        viewModelScope.launch {
            val pin = _state.value.setupPinCode
            val confirm = _state.value.setupPinConfirm

            if (pin.length != 4) {
                _state.value = _state.value.copy(
                    error = "PIN code must be 4 digits"
                )
                return@launch
            }

            if (pin != confirm) {
                _state.value = _state.value.copy(
                    error = "PIN codes do not match"
                )
                return@launch
            }

            _state.value = _state.value.copy(isSettingUpPin = true, error = null)

            when (val result = profileRepository.setupPinCode(pin)) {
                is ApiResponse.Success -> {
                    // Reload user data to get updated hasPinCode status
                    loadUserData()
                    _state.value = _state.value.copy(
                        isSettingUpPin = false,
                        showSetupPinDialog = false,
                        setupPinCode = "",
                        setupPinConfirm = ""
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isSettingUpPin = false,
                        error = result.message
                    )
                }

                is ApiResponse.Loading -> {}
            }
        }
    }

    fun closeSetupPinDialog() {
        _state.value = _state.value.copy(
            showSetupPinDialog = false,
            setupPinCode = "",
            setupPinConfirm = "",
            error = null
        )
    }
}
