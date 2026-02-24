package com.cirin0.worktimetracker.features.workschedule.data.model

import com.google.gson.annotations.SerializedName

data class WorkScheduleResponse(
    val message: String,
    val data: WorkSchedule?
)

data class WorkSchedule(
    val id: Int,
    val name: String,
    @SerializedName("is_default") val isDefault: Boolean,
    @SerializedName("daily_schedules") val dailySchedules: List<DailySchedule>?
)

data class DailySchedule(
    val id: Int,
    @SerializedName("day_of_week") val dayOfWeek: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("break_duration") val breakDuration: Int,
    @SerializedName("is_working_day") val isWorkingDay: Boolean
) {
    fun getDayOrder(): Int {
        return when (dayOfWeek.lowercase()) {
            "monday" -> 1
            "tuesday" -> 2
            "wednesday" -> 3
            "thursday" -> 4
            "friday" -> 5
            "saturday" -> 6
            "sunday" -> 7
            else -> 8
        }
    }
}



