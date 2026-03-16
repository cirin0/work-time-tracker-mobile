package com.cirin0.worktimetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cirin0.worktimetracker.core.database.entity.CachedTimesheetEntity

@Dao
interface TimesheetDao {
    @Query("SELECT * FROM cached_timesheet WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getByMonth(year: Int, month: Int): CachedTimesheetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CachedTimesheetEntity>)

    @Query("DELETE FROM cached_timesheet WHERE cachedAt < :minCachedAt")
    suspend fun clearOld(minCachedAt: Long)

    @Query("DELETE FROM cached_timesheet")
    suspend fun clearCache()
}

