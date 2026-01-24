package com.cirin0.worktimetracker.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentTypeInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // For PATCH requests, ensure proper headers
        val request = if (originalRequest.method == "PATCH") {
            originalRequest.newBuilder()
                .header("Accept", "application/json")
                .removeHeader("Content-Type") // Remove auto-generated Content-Type
                .header("Content-Type", "application/json") // Add clean Content-Type
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
