package com.cirin0.worktimetracker.core.database.entity

import androidx.room.Entity
import com.cirin0.worktimetracker.features.timesheet.data.model.AttendanceStats
import com.cirin0.worktimetracker.features.timesheet.data.model.PeriodSummary
import com.cirin0.worktimetracker.features.timesheet.data.model.SummaryPeriods
import com.cirin0.worktimetracker.features.timesheet.data.model.TimeSummary

@Entity(
    tableName = "cached_timesheet",
    primaryKeys = ["year", "month"]
)
data class CachedTimesheetEntity(
    val year: Int,
    val month: Int,
    val userId: Int,
    val totalHours: Int,
    val totalMinutes: Int,
    val workingDays: Int,
    val averageWorkTime: Int,
    val lateCount: Int,
    val earlyCount: Int,
    val onTimeCount: Int,
    val totalLateMinutes: Int,
    val averageLateMinutes: Double,
    val earlyLeaveCount: Int,
    val totalEarlyLeaveMinutes: Int,
    val averageEarlyLeaveMinutes: Double,
    val overtimeCount: Int,
    val totalOvertimeMinutes: Int,
    val averageOvertimeMinutes: Double,
    val todayHours: Int,
    val todayMinutes: Int,
    val todayWorkingDays: Int,
    val todayLateCount: Int,
    val todayEarlyCount: Int,
    val weekHours: Int,
    val weekMinutes: Int,
    val weekWorkingDays: Int,
    val weekLateCount: Int,
    val weekEarlyCount: Int,
    val monthHours: Int,
    val monthMinutes: Int,
    val monthWorkingDays: Int,
    val monthLateCount: Int,
    val monthEarlyCount: Int,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedTimesheetEntity.toTimeSummary(): TimeSummary {
    return TimeSummary(
        userId = userId,
        totalHours = totalHours,
        totalMinutes = totalMinutes,
        workingDays = workingDays,
        averageWorkTime = averageWorkTime,
        attendance = AttendanceStats(
            lateCount = lateCount,
            earlyCount = earlyCount,
            onTimeCount = onTimeCount,
            totalLateMinutes = totalLateMinutes,
            averageLateMinutes = averageLateMinutes,
            earlyLeaveCount = earlyLeaveCount,
            totalEarlyLeaveMinutes = totalEarlyLeaveMinutes,
            averageEarlyLeaveMinutes = averageEarlyLeaveMinutes,
            overtimeCount = overtimeCount,
            totalOvertimeMinutes = totalOvertimeMinutes,
            averageOvertimeMinutes = averageOvertimeMinutes
        ),
        summary = SummaryPeriods(
            today = PeriodSummary(
                hours = todayHours,
                minutes = todayMinutes,
                workingDays = todayWorkingDays,
                lateCount = todayLateCount,
                earlyCount = todayEarlyCount
            ),
            week = PeriodSummary(
                hours = weekHours,
                minutes = weekMinutes,
                workingDays = weekWorkingDays,
                lateCount = weekLateCount,
                earlyCount = weekEarlyCount
            ),
            month = PeriodSummary(
                hours = monthHours,
                minutes = monthMinutes,
                workingDays = monthWorkingDays,
                lateCount = monthLateCount,
                earlyCount = monthEarlyCount
            )
        )
    )
}

fun TimeSummary.toCachedEntity(year: Int, month: Int): CachedTimesheetEntity {
    return CachedTimesheetEntity(
        year = year,
        month = month,
        userId = userId,
        totalHours = totalHours,
        totalMinutes = totalMinutes,
        workingDays = workingDays,
        averageWorkTime = averageWorkTime,
        lateCount = attendance.lateCount,
        earlyCount = attendance.earlyCount,
        onTimeCount = attendance.onTimeCount,
        totalLateMinutes = attendance.totalLateMinutes,
        averageLateMinutes = attendance.averageLateMinutes,
        earlyLeaveCount = attendance.earlyLeaveCount,
        totalEarlyLeaveMinutes = attendance.totalEarlyLeaveMinutes,
        averageEarlyLeaveMinutes = attendance.averageEarlyLeaveMinutes,
        overtimeCount = attendance.overtimeCount,
        totalOvertimeMinutes = attendance.totalOvertimeMinutes,
        averageOvertimeMinutes = attendance.averageOvertimeMinutes,
        todayHours = summary.today.hours,
        todayMinutes = summary.today.minutes,
        todayWorkingDays = summary.today.workingDays,
        todayLateCount = summary.today.lateCount,
        todayEarlyCount = summary.today.earlyCount,
        weekHours = summary.week.hours,
        weekMinutes = summary.week.minutes,
        weekWorkingDays = summary.week.workingDays,
        weekLateCount = summary.week.lateCount,
        weekEarlyCount = summary.week.earlyCount,
        monthHours = summary.month.hours,
        monthMinutes = summary.month.minutes,
        monthWorkingDays = summary.month.workingDays,
        monthLateCount = summary.month.lateCount,
        monthEarlyCount = summary.month.earlyCount
    )
}

