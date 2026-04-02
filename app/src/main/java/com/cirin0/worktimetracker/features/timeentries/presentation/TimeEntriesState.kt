package com.cirin0.worktimetracker.features.timeentries.presentation

import com.cirin0.worktimetracker.features.profile.data.model.User
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry
import com.cirin0.worktimetracker.features.workschedule.data.model.WorkSchedule

enum class TimeEntriesUiError {
    PIN_LENGTH,
    PIN_DIGITS_ONLY
}

data class TimeEntriesState(
    val activeEntry: TimeEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val startComment: String = "",
    val stopComment: String = "",
    val pinCode: String = "",
    val uiError: TimeEntriesUiError? = null,
    val timeEntries: List<TimeEntry> = emptyList(),
    val isLoadingList: Boolean = false,
    val listError: String? = null,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
    val isLoadingLocation: Boolean = false,
    val locationPermissionDenied: Boolean = false,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val user: User? = null,
    val isInOffice: Boolean = false,
    val showQRScanner: Boolean = false,
    val qrCodeScanned: String? = null,
    val cameraPermissionDenied: Boolean = false,
    val qrCodeScanSuccess: Boolean = false,
    val showServerUnavailableWarning: Boolean = false,
    val workSchedule: WorkSchedule? = null,
    val targetHours: Float = 8f,
    val isWorkingDay: Boolean = true,
    val showSetupPinDialog: Boolean = false,
    val setupPinCode: String = "",
    val setupPinConfirm: String = "",
    val isSettingUpPin: Boolean = false
)
