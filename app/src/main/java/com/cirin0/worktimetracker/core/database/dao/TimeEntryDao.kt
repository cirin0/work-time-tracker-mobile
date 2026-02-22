package com.cirin0.worktimetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cirin0.worktimetracker.core.database.entity.CachedTimeEntryEntity

@Dao
interface TimeEntryDao {
    @Query("SELECT * FROM cached_time_entries ORDER BY startTime DESC")
    suspend fun getAllCachedTimeEntries(): List<CachedTimeEntryEntity>

    @Query("SELECT * FROM cached_time_entries WHERE id = :id")
    suspend fun getCachedTimeEntryById(id: Int): CachedTimeEntryEntity?

    @Query("SELECT * FROM cached_time_entries WHERE stopTime IS NULL LIMIT 1")
    suspend fun getActiveTimeEntry(): CachedTimeEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheTimeEntry(entry: CachedTimeEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheTimeEntries(entries: List<CachedTimeEntryEntity>)

    @Query("DELETE FROM cached_time_entries")
    suspend fun clearCache()

    @Query("DELETE FROM cached_time_entries WHERE id = :id")
    suspend fun deleteCachedTimeEntry(id: Int)
}
