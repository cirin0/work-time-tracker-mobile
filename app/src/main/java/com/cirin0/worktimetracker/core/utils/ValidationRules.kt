package com.cirin0.worktimetracker.core.utils

import android.util.Patterns

object ValidationRules {

    fun isValidEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email не може бути порожнім")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Error("Невалідний формат email")

            else -> ValidationResult.Success
        }
    }

    fun isValidPassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Пароль не може бути порожнім")
//            password.length < 8 -> ValidationResult.Error("Мінімум 8 символів")
//            !password.any { it.isDigit() } -> ValidationResult.Error("Потрібна хоча б одна цифра")
//            !password.any { it.isUpperCase() } -> ValidationResult.Error("Потрібна велика літера")
            else -> ValidationResult.Success
        }
    }

    fun isValidName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Ім'я не може бути порожнім")
            name.length < 2 -> ValidationResult.Error("Ім'я занадто коротке")
            !name.all { it.isLetter() || it.isWhitespace() } ->
                ValidationResult.Error("Ім'я може містити лише літери")

            else -> ValidationResult.Success
        }
    }

    fun isValidPhone(phone: String): ValidationResult {
        return when {
            phone.isBlank() -> ValidationResult.Error("Телефон не може бути порожнім")
            !phone.matches(Regex("^\\+?[0-9]{10,13}$")) ->
                ValidationResult.Error("Невалідний формат телефону")

            else -> ValidationResult.Success
        }
    }

    fun isValidPinCode(pin: String): ValidationResult {
        return when {
            pin.isBlank() -> ValidationResult.Error("PIN-код не може бути порожнім")
            pin.length != Constants.Validation.PIN_CODE_LENGTH ->
                ValidationResult.Error("PIN-код має містити ${Constants.Validation.PIN_CODE_LENGTH} цифри")

            !pin.all { it.isDigit() } -> ValidationResult.Error("PIN-код має містити тільки цифри")
            else -> ValidationResult.Success
        }
    }

    fun isValidComment(
        comment: String,
        minLength: Int = Constants.Validation.MIN_COMMENT_LENGTH,
        maxLength: Int = Constants.Validation.MAX_COMMENT_LENGTH,
        required: Boolean = true
    ): ValidationResult {
        return when {
            comment.isBlank() && required -> ValidationResult.Error("Коментар не може бути порожнім")
            comment.isNotBlank() && comment.length < minLength ->
                ValidationResult.Error("Коментар занадто короткий (мін. $minLength символів)")

            comment.length > maxLength ->
                ValidationResult.Error("Коментар занадто довгий (макс. $maxLength символів)")

            else -> ValidationResult.Success
        }
    }

    fun isValidTextLength(
        text: String,
        fieldName: String = "Поле",
        minLength: Int = Constants.Validation.MIN_NAME_LENGTH,
        maxLength: Int = Constants.Validation.MAX_NAME_LENGTH,
        required: Boolean = true
    ): ValidationResult {
        return when {
            text.isBlank() && required -> ValidationResult.Error("$fieldName не може бути порожнім")
            text.isNotBlank() && text.length < minLength ->
                ValidationResult.Error("$fieldName занадто короткий (мін. $minLength символів)")

            text.length > maxLength ->
                ValidationResult.Error("$fieldName занадто довгий (макс. $maxLength символів)")

            else -> ValidationResult.Success
        }
    }

    fun isValidNumber(number: String, min: Int = 0, max: Int = Int.MAX_VALUE): ValidationResult {
        return when {
            number.isBlank() -> ValidationResult.Error("Число не може бути порожнім")
            number.toIntOrNull() == null -> ValidationResult.Error("Введіть коректне число")
            number.toInt() < min -> ValidationResult.Error("Мінімальне значення: $min")
            number.toInt() > max -> ValidationResult.Error("Максимальне значення: $max")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()

    val isValid: Boolean get() = this is Success
    val errorMessage: String? get() = (this as? Error)?.message
}

