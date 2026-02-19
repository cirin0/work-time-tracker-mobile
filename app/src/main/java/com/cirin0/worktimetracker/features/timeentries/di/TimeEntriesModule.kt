package com.cirin0.worktimetracker.features.timeentries.di

import com.cirin0.worktimetracker.features.timeentries.data.api.TimeEntriesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object TimeEntriesModule {
    @Provides
    @Singleton
    fun provideTimeEntriesApi(retrofit: Retrofit): TimeEntriesApi {
        return retrofit.create(TimeEntriesApi::class.java)
    }
}