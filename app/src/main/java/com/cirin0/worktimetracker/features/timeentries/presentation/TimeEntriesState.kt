package com.cirin0.worktimetracker.features.timeentries.presentation

import com.cirin0.worktimetracker.features.profile.data.model.User
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry

data class TimeEntriesState(
    val activeEntry: TimeEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val startComment: String = "",
    val stopComment: String = "",
    val pinCode: String = "",
    val timeEntries: List<TimeEntry> = emptyList(),
    val isLoadingList: Boolean = false,
    val listError: String? = null,
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
    val showServerUnavailableWarning: Boolean = false
)

