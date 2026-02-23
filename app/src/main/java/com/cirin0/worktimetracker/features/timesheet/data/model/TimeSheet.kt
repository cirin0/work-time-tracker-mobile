package com.cirin0.worktimetracker.features.timesheet.data.model

import com.google.gson.annotations.SerializedName

data class TimeSummary(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("total_hours") val totalHours: Int,
    @SerializedName("total_minutes") val totalMinutes: Int,
    @SerializedName("entries_count") val entriesCount: Int,
    @SerializedName("average_work_time") val averageWorkTime: Int,
    val summary: SummaryPeriods
)

data class SummaryPeriods(
    val today: PeriodSummary,
    val week: PeriodSummary,
    val month: PeriodSummary
)

data class PeriodSummary(
    val hours: Int,
    val minutes: Int,
    val entries: Int
)

data class TimeSummaryResponse(
    val message: String,
    val data: TimeSummary
)

