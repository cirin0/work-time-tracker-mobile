package com.cirin0.worktimetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cirin0.worktimetracker.core.database.entity.CachedWorkScheduleEntity

@Dao
interface WorkScheduleDao {
    @Query("SELECT * FROM cached_work_schedule WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getByMonth(year: Int, month: Int): CachedWorkScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: CachedWorkScheduleEntity)

    @Query("DELETE FROM cached_work_schedule WHERE cachedAt < :minCachedAt")
    suspend fun clearOld(minCachedAt: Long)

    @Query("DELETE FROM cached_work_schedule")
    suspend fun clearCache()
}

