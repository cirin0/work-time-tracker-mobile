package com.cirin0.worktimetracker.features.manager.di

import com.cirin0.worktimetracker.features.manager.data.api.ManagerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {
    @Provides
    @Singleton
    fun provideManagerApi(retrofit: Retrofit): ManagerApi {
        return retrofit.create(ManagerApi::class.java)
    }
}
