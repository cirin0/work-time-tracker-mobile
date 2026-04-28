package com.cirin0.worktimetracker.core.utils

import android.content.Context
import android.util.Patterns
import androidx.annotation.StringRes
import com.cirin0.worktimetracker.R

object ValidationRules {

    fun isValidEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error(R.string.validation_email_required)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Error(R.string.validation_email_invalid)

            else -> ValidationResult.Success
        }
    }

    fun isValidPassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error(R.string.validation_password_required)
//            password.length < 8 -> ValidationResult.Error("Мінімум 8 символів")
//            !password.any { it.isDigit() } -> ValidationResult.Error("Потрібна хоча б одна цифра")
//            !password.any { it.isUpperCase() } -> ValidationResult.Error("Потрібна велика літера")
            else -> ValidationResult.Success
        }
    }

    fun isValidName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error(R.string.validation_name_required)
            name.length < 2 -> ValidationResult.Error(R.string.validation_name_too_short)
            !name.all { it.isLetter() || it.isWhitespace() || it == '\'' || it == '’' || it == 'ʼ' || it == '-' } ->
                ValidationResult.Error(R.string.validation_name_letters_only)

            else -> ValidationResult.Success
        }
    }

    fun isValidPhone(phone: String): ValidationResult {
        return when {
            phone.isBlank() -> ValidationResult.Error(R.string.validation_phone_required)
            !phone.matches(Regex("^\\+?[0-9]{10,13}$")) ->
                ValidationResult.Error(R.string.validation_phone_invalid)

            else -> ValidationResult.Success
        }
    }

    fun isValidPinCode(pin: String): ValidationResult {
        return when {
            pin.isBlank() -> ValidationResult.Error(R.string.validation_pin_required)
            pin.length != Constants.Validation.PIN_CODE_LENGTH ->
                ValidationResult.Error(
                    R.string.validation_pin_exact_digits,
                    listOf(Constants.Validation.PIN_CODE_LENGTH)
                )

            !pin.all { it.isDigit() } -> ValidationResult.Error(R.string.validation_pin_digits_only)
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
            comment.isBlank() && required -> ValidationResult.Error(R.string.validation_comment_required)
            comment.isNotBlank() && comment.length < minLength ->
                ValidationResult.Error(R.string.validation_comment_too_short, listOf(minLength))

            comment.length > maxLength ->
                ValidationResult.Error(R.string.validation_comment_too_long, listOf(maxLength))

            else -> ValidationResult.Success
        }
    }

    fun isValidTextLength(
        text: String,
        fieldName: String = "Field",
        minLength: Int = Constants.Validation.MIN_NAME_LENGTH,
        maxLength: Int = Constants.Validation.MAX_NAME_LENGTH,
        required: Boolean = true
    ): ValidationResult {
        return when {
            text.isBlank() && required ->
                ValidationResult.Error(R.string.validation_field_required, listOf(fieldName))

            text.isNotBlank() && text.length < minLength ->
                ValidationResult.Error(
                    R.string.validation_field_too_short,
                    listOf(fieldName, minLength)
                )

            text.length > maxLength ->
                ValidationResult.Error(
                    R.string.validation_field_too_long,
                    listOf(fieldName, maxLength)
                )

            else -> ValidationResult.Success
        }
    }

    fun isValidNumber(number: String, min: Int = 0, max: Int = Int.MAX_VALUE): ValidationResult {
        return when {
            number.isBlank() -> ValidationResult.Error(R.string.validation_number_required)
            number.toIntOrNull() == null -> ValidationResult.Error(R.string.validation_number_invalid)
            number.toInt() < min -> ValidationResult.Error(
                R.string.validation_number_min,
                listOf(min)
            )

            number.toInt() > max -> ValidationResult.Error(
                R.string.validation_number_max,
                listOf(max)
            )

            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(
        @param:StringRes val messageResId: Int,
        val formatArgs: List<Any> = emptyList()
    ) : ValidationResult() {
        fun resolve(context: Context): String {
            return if (formatArgs.isEmpty()) {
                context.getString(messageResId)
            } else {
                context.getString(messageResId, *formatArgs.toTypedArray())
            }
        }
    }

    val isValid: Boolean get() = this is Success
}

