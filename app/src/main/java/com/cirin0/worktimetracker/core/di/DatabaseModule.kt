package com.cirin0.worktimetracker.core.di

import android.content.Context
import androidx.room.Room
import com.cirin0.worktimetracker.core.database.AppDatabase
import com.cirin0.worktimetracker.core.database.dao.CompanyDao
import com.cirin0.worktimetracker.core.database.dao.LeaveRequestDao
import com.cirin0.worktimetracker.core.database.dao.TimeEntryDao
import com.cirin0.worktimetracker.core.database.dao.TimesheetDao
import com.cirin0.worktimetracker.core.database.dao.UserDao
import com.cirin0.worktimetracker.core.database.dao.WorkScheduleDao
import com.cirin0.worktimetracker.core.utils.ConnectivityObserver
import com.cirin0.worktimetracker.core.utils.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "work_time_tracker_db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideTimeEntryDao(database: AppDatabase): TimeEntryDao {
        return database.timeEntryDao()
    }

    @Provides
    @Singleton
    fun provideTimesheetDao(database: AppDatabase): TimesheetDao {
        return database.timesheetDao()
    }

    @Provides
    @Singleton
    fun provideCompanyDao(database: AppDatabase): CompanyDao {
        return database.companyDao()
    }

    @Provides
    @Singleton
    fun provideWorkScheduleDao(database: AppDatabase): WorkScheduleDao {
        return database.workScheduleDao()
    }

    @Provides
    @Singleton
    fun provideLeaveRequestDao(database: AppDatabase): LeaveRequestDao {
        return database.leaveRequestDao()
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }
}
