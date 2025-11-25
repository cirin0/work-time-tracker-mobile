package com.cirin0.worktimetracker.core.di

import com.cirin0.worktimetracker.core.network.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val LOCAL_URL = "http://10.0.2.2:8000/api/"
    private const val REMOTE_URL = "http://localhost:8000/api/"

    @LocalUrl
    @Provides
    fun provideLocalUrl(): String = LOCAL_URL

    @RemoteUrl
    @Provides
    fun provideRemoteUrl(): String = REMOTE_URL

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
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Interceptors
            addInterceptor(authInterceptor)
            addInterceptor(loggingInterceptor)

            // Timeouts
            connectTimeout(30, TimeUnit.SECONDS)
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
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
        @LocalUrl baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }
}