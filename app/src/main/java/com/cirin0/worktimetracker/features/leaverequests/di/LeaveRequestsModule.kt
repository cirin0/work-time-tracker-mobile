package com.cirin0.worktimetracker.features.leaverequests.di

import com.cirin0.worktimetracker.features.leaverequests.data.api.LeaveRequestsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LeaveRequestsModule {

    @Provides
    @Singleton
    fun provideLeaveRequestsApi(retrofit: Retrofit): LeaveRequestsApi {
        return retrofit.create(LeaveRequestsApi::class.java)
    }
}

