package com.cirin0.worktimetracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cirin0.worktimetracker.core.database.dao.UserDao
import com.cirin0.worktimetracker.core.database.entity.CachedUserEntity

@Database(
    entities = [CachedUserEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
