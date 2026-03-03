package com.cirin0.worktimetracker.core.utils

import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Maps HTTP error codes to user-friendly Ukrainian messages
 */
@Singleton
class ErrorMapper @Inject constructor() {

    fun mapHttpError(code: Int, defaultMessage: String? = null): String {
        return when (code) {
            400 -> "Неправильні дані. Перевірте введену інформацію"
            401 -> "Потрібна авторизація. Увійдіть знову"
            403 -> "Доступ заборонено"
            404 -> "Дані не знайдено"
            422 -> "Помилка валідації даних"
            500 -> "Помилка сервера. Спробуйте пізніше"
            502 -> "Сервер тимчасово недоступний"
            503 -> "Сервіс недоступний. Спробуйте пізніше"
            504 -> "Час очікування відповіді сервера вичерпано"
            else -> defaultMessage ?: "Помилка з'єднання (код: $code)"
        }
    }

    fun mapNetworkError(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException -> "Немає з'єднання з сервером"
            is java.net.SocketTimeoutException -> "Час очікування вичерпано"
            is java.net.ConnectException -> "Не вдалося підключитися до сервера"
            is javax.net.ssl.SSLException -> "Помилка безпечного з'єднання"
            is java.io.IOException -> "Помилка мережі"
            else -> throwable.message ?: "Невідома помилка"
        }
    }
}

