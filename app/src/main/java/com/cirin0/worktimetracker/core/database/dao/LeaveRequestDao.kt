package com.cirin0.worktimetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cirin0.worktimetracker.core.database.entity.CachedLeaveRequestEntity

@Dao
interface LeaveRequestDao {
    @Query("SELECT * FROM cached_leave_requests ORDER BY createdAt DESC")
    suspend fun getAll(): List<CachedLeaveRequestEntity>

    @Query("SELECT * FROM cached_leave_requests WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): CachedLeaveRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedLeaveRequestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CachedLeaveRequestEntity)

    @Query("DELETE FROM cached_leave_requests")
    suspend fun clearCache()
}

