package com.cirin0.worktimetracker.features.timeentries.presentation

data class WorkProgressUiState(
    val workedMinutes: Int = 0,
    val targetHours: Float = 8f,
    val isWorkingDay: Boolean = true,
    val isTracking: Boolean = false,
) {
    val workedHours: Float
        get() = workedMinutes / 60f

    val progress: Float
        get() = (workedHours / targetHours.coerceAtLeast(1f)).coerceIn(0f, 1f)

    val remainingMinutes: Int
        get() = ((targetHours - workedHours) * 60).toInt().coerceAtLeast(0)
}

