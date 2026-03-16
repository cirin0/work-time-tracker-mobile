package com.cirin0.worktimetracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cirin0.worktimetracker.features.workschedule.data.model.DailySchedule
import com.cirin0.worktimetracker.features.workschedule.data.model.WorkSchedule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "cached_work_schedule")
data class CachedWorkScheduleEntity(
    @PrimaryKey
    val periodKey: String,
    val year: Int,
    val month: Int,
    val scheduleId: Int,
    val name: String,
    val isDefault: Boolean,
    val dailySchedulesJson: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

private val gson = Gson()

fun CachedWorkScheduleEntity.toWorkSchedule(): WorkSchedule {
    val schedulesType = object : TypeToken<List<DailySchedule>>() {}.type
    val dailySchedules = dailySchedulesJson?.let {
        gson.fromJson<List<DailySchedule>>(it, schedulesType)
    }

    return WorkSchedule(
        id = scheduleId,
        name = name,
        isDefault = isDefault,
        dailySchedules = dailySchedules
    )
}

fun WorkSchedule.toCachedEntity(year: Int, month: Int): CachedWorkScheduleEntity {
    return CachedWorkScheduleEntity(
        periodKey = "$year-$month",
        year = year,
        month = month,
        scheduleId = id,
        name = name,
        isDefault = isDefault,
        dailySchedulesJson = dailySchedules?.let { gson.toJson(it) }
    )
}

