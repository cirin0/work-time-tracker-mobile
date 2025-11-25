package com.cirin0.worktimetracker.core.network

sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}

suspend fun <T> apiCall(call: suspend () -> T): ApiResponse<T> {
    return try {
        ApiResponse.Success(call())
    } catch (e: Exception) {
        ApiResponse.Error(e.message ?: "Unknown error occurred")
    }
}