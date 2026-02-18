package com.cirin0.worktimetracker.core.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

sealed class ApiResponse<out T> {
    data class Success<T>(val data: T, val fromCache: Boolean = false) : ApiResponse<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val errors: Map<String, List<String>>? = null
    ) : ApiResponse<Nothing>()

    object Loading : ApiResponse<Nothing>()
}

data class ErrorResponse(
    val message: String,
    val errors: Map<String, List<String>>? = null
)

suspend fun <T> apiCall(call: suspend () -> T): ApiResponse<T> {
    return try {
        ApiResponse.Success(call())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val errorResponse = try {
            Gson().fromJson(errorBody, ErrorResponse::class.java)
        } catch (inner: Exception) {
            null
        }
        ApiResponse.Error(
            message = errorResponse?.message ?: e.message(),
            code = e.code(),
            errors = errorResponse?.errors
        )
    } catch (e: IOException) {
        ApiResponse.Error("Network error: ${e.message}")
    } catch (e: Exception) {
        ApiResponse.Error(e.message ?: "Unknown error occurred")
    }
}