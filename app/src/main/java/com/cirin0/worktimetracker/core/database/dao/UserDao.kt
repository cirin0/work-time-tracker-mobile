package com.cirin0.worktimetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cirin0.worktimetracker.core.database.entity.CachedUserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM cached_user LIMIT 1")
    suspend fun getCachedUser(): CachedUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheUser(user: CachedUserEntity)

    @Query("DELETE FROM cached_user")
    suspend fun clearCache()
}
