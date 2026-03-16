package com.cirin0.worktimetracker.core.utils

import com.cirin0.worktimetracker.core.localization.AppLocaleManager
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Maps HTTP error codes to user-friendly Ukrainian messages
 */
@Singleton
class ErrorMapper @Inject constructor() {
    private fun localized(uk: String, en: String): String {
        return if (AppLocaleManager.getCurrentLanguage() == AppLocaleManager.DEFAULT_LANGUAGE) uk else en
    }

    fun mapHttpError(code: Int, defaultMessage: String? = null): String {
        return when (code) {
            400 -> localized(
                "Неправильні дані. Перевірте введену інформацію",
                "Invalid data. Check input"
            )

            401 -> localized(
                "Потрібна авторизація. Увійдіть знову",
                "Authorization required. Sign in again"
            )

            403 -> localized("Доступ заборонено", "Access denied")
            404 -> localized("Дані не знайдено", "Data not found")
            422 -> localized("Помилка валідації даних", "Validation error")
            500 -> localized("Помилка сервера. Спробуйте пізніше", "Server error. Try again later")
            502 -> localized("Сервер тимчасово недоступний", "Server is temporarily unavailable")
            503 -> localized(
                "Сервіс недоступний. Спробуйте пізніше",
                "Service unavailable. Try again later"
            )

            504 -> localized(
                "Час очікування відповіді сервера вичерпано",
                "Server response timeout"
            )

            else -> defaultMessage ?: localized(
                "Помилка з'єднання (код: $code)",
                "Connection error (code: $code)"
            )
        }
    }

    fun mapNetworkError(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException -> localized(
                "Немає з'єднання з сервером",
                "No connection to server"
            )

            is java.net.SocketTimeoutException -> localized(
                "Час очікування вичерпано",
                "Request timeout"
            )

            is java.net.ConnectException -> localized(
                "Не вдалося підключитися до сервера",
                "Could not connect to server"
            )

            is javax.net.ssl.SSLException -> localized(
                "Помилка безпечного з'єднання",
                "Secure connection error"
            )

            is java.io.IOException -> localized("Помилка мережі", "Network error")
            else -> throwable.message ?: localized("Невідома помилка", "Unknown error")
        }
    }
}

