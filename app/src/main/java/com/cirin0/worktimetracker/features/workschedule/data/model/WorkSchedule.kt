package com.cirin0.worktimetracker.features.workschedule.data.model

import com.cirin0.worktimetracker.core.utils.DateUtils
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
        return DateUtils.getDayOrder(dayOfWeek)
    }
}



