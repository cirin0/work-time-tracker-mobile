package com.cirin0.worktimetracker.features.timesheet.data.model

import com.google.gson.annotations.SerializedName

data class TimeSummary(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("total_hours") val totalHours: Int,
    @SerializedName("total_minutes") val totalMinutes: Int,
    @SerializedName("working_days") val workingDays: Int,
    @SerializedName("average_work_time") val averageWorkTime: Int,
    @SerializedName("attendance") val attendance: AttendanceStats,
    val summary: SummaryPeriods
)

data class AttendanceStats(
    @SerializedName("late_count") val lateCount: Int,
    @SerializedName("early_count") val earlyCount: Int,
    @SerializedName("on_time_count") val onTimeCount: Int,
    @SerializedName("total_late_minutes") val totalLateMinutes: Int,
    @SerializedName("average_late_minutes") val averageLateMinutes: Double,
    @SerializedName("early_leave_count") val earlyLeaveCount: Int,
    @SerializedName("total_early_leave_minutes") val totalEarlyLeaveMinutes: Int,
    @SerializedName("average_early_leave_minutes") val averageEarlyLeaveMinutes: Double,
    @SerializedName("overtime_count") val overtimeCount: Int,
    @SerializedName("total_overtime_minutes") val totalOvertimeMinutes: Int,
    @SerializedName("average_overtime_minutes") val averageOvertimeMinutes: Double
)

data class SummaryPeriods(
    val today: PeriodSummary,
    val week: PeriodSummary,
    val month: PeriodSummary
)

data class PeriodSummary(
    val hours: Int,
    val minutes: Int,
    @SerializedName("working_days") val workingDays: Int,
    @SerializedName("late_count") val lateCount: Int,
    @SerializedName("early_count") val earlyCount: Int
)

data class TimeSummaryResponse(
    val message: String,
    val data: TimeSummary
)

