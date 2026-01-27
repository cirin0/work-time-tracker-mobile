package com.cirin0.worktimetracker.features.company.di

import com.cirin0.worktimetracker.features.company.data.api.CompanyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object CompanyModule {
    @Provides
    @Singleton
    fun provideCompanyApi(retrofit: Retrofit): CompanyApi {
        return retrofit.create(CompanyApi::class.java)
    }
}
