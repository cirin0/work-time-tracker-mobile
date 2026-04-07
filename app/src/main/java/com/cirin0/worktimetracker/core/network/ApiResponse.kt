package com.cirin0.worktimetracker.core.network

import com.cirin0.worktimetracker.core.localization.AppLocaleManager
import com.cirin0.worktimetracker.core.utils.ErrorMapper
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null
)

private val errorMapper = ErrorMapper()
private val gson = Gson()

private fun localizedUnknownError(): String {
    return if (AppLocaleManager.getCurrentLanguage() == AppLocaleManager.DEFAULT_LANGUAGE) {
        "Невідома помилка"
    } else {
        "Unknown error"
    }
}

suspend fun <T> apiCall(call: suspend () -> T): ApiResponse<T> {
    return try {
        ApiResponse.Success(call())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val errorResponse = try {
            gson.fromJson(errorBody, ErrorResponse::class.java)
        } catch (_: Exception) {
            null
        }

        val userMessage = errorResponse?.message
            ?: errorResponse?.error
            ?: errorMapper.mapHttpError(e.code(), e.message())

        ApiResponse.Error(
            message = userMessage,
            code = e.code(),
            errors = errorResponse?.errors
        )
    } catch (e: IOException) {
        ApiResponse.Error(errorMapper.mapNetworkError(e))
    } catch (e: Exception) {
        ApiResponse.Error(e.message ?: localizedUnknownError())
    }
}