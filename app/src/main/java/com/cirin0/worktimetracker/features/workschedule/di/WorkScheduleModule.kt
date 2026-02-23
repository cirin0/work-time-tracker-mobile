package com.cirin0.worktimetracker.features.workschedule.di

import com.cirin0.worktimetracker.features.workschedule.data.api.WorkScheduleApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkScheduleModule {

    @Provides
    @Singleton
    fun provideWorkScheduleApi(retrofit: Retrofit): WorkScheduleApi {
        return retrofit.create(WorkScheduleApi::class.java)
    }
}

