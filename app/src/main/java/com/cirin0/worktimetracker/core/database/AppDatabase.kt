package com.cirin0.worktimetracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cirin0.worktimetracker.core.database.dao.CompanyDao
import com.cirin0.worktimetracker.core.database.dao.LeaveRequestDao
import com.cirin0.worktimetracker.core.database.dao.TimeEntryDao
import com.cirin0.worktimetracker.core.database.dao.TimesheetDao
import com.cirin0.worktimetracker.core.database.dao.UserDao
import com.cirin0.worktimetracker.core.database.dao.WorkScheduleDao
import com.cirin0.worktimetracker.core.database.entity.CachedCompanyEntity
import com.cirin0.worktimetracker.core.database.entity.CachedLeaveRequestEntity
import com.cirin0.worktimetracker.core.database.entity.CachedTimeEntryEntity
import com.cirin0.worktimetracker.core.database.entity.CachedTimesheetEntity
import com.cirin0.worktimetracker.core.database.entity.CachedUserEntity
import com.cirin0.worktimetracker.core.database.entity.CachedWorkScheduleEntity

@Database(
    entities = [
        CachedUserEntity::class,
        CachedTimeEntryEntity::class,
        CachedTimesheetEntity::class,
        CachedCompanyEntity::class,
        CachedWorkScheduleEntity::class,
        CachedLeaveRequestEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun timeEntryDao(): TimeEntryDao
    abstract fun timesheetDao(): TimesheetDao
    abstract fun companyDao(): CompanyDao
    abstract fun workScheduleDao(): WorkScheduleDao
    abstract fun leaveRequestDao(): LeaveRequestDao
}
