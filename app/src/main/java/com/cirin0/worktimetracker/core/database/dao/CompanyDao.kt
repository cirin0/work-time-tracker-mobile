package com.cirin0.worktimetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cirin0.worktimetracker.core.database.entity.CachedCompanyEntity

@Dao
interface CompanyDao {
    @Query("SELECT * FROM cached_company LIMIT 1")
    suspend fun getCompany(): CachedCompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CachedCompanyEntity)

    @Query("DELETE FROM cached_company")
    suspend fun clearCache()
}

