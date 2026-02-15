package com.cirin0.worktimetracker.core.di

import com.cirin0.worktimetracker.core.network.AuthInterceptor
import com.cirin0.worktimetracker.core.network.ContentTypeInterceptor
import com.cirin0.worktimetracker.core.network.TokenRefreshInterceptor
import com.cirin0.worktimetracker.core.utils.Constants
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Named
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val LOCAL_DOMAIN = "http://192.168.0.52:8000"
    private const val REMOTE_DOMAIN = "https://work-time-tracker-api-cpdeb7e7b9axazd0.swedencentral-01.azurewebsites.net"

    @Provides
    @Singleton
    @Named("active_domain")
    fun provideActiveDomain(): String = LOCAL_DOMAIN

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        contentTypeInterceptor: ContentTypeInterceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Interceptors
            addInterceptor(authInterceptor)
            addInterceptor(tokenRefreshInterceptor)
            addInterceptor(contentTypeInterceptor)
            addInterceptor(loggingInterceptor)

            // Timeouts
            connectTimeout(10, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)

            // Retry
            retryOnConnectionFailure(true)
        }.build()
    }

    @Provides
    @Singleton
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
        @Named("active_domain") domain: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("$domain/api/")
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Provides
    @Named(Constants.NAMED_IMAGE_URL)
    fun provideImageBaseUrl(@Named("active_domain") domain: String): String = domain
}