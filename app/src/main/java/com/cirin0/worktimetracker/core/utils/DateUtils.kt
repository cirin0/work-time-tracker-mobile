package com.cirin0.worktimetracker.core.utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {
    /**
     * Format a date string from server to display date (dd.MM.yyyy)
     */
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "-"

        return try {
            val date = when {
                dateString.contains('T') -> SimpleDateFormat(
                    "dd.MM.yyyy'T'HH:mm:ss",
                    Locale.getDefault()
                ).parse(dateString)

                dateString.contains(':') -> SimpleDateFormat(
                    "dd.MM.yyyy HH:mm:ss",
                    Locale.getDefault()
                ).parse(dateString)

                else -> SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateString)
            }

            date?.let { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it) }
                ?: dateString
        } catch (_: Exception) {
            dateString
        }
    }

    /**
     * Format a date-time string from server to display date-time (dd.MM.yyyy HH:mm:ss)
     */
    fun formatDateTime(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "-"

        return try {
            val date = when {
                dateTimeString.contains('T') -> SimpleDateFormat(
                    "dd.MM.yyyy'T'HH:mm:ss",
                    Locale.getDefault()
                ).parse(dateTimeString)

                dateTimeString.contains(':') -> SimpleDateFormat(
                    "dd.MM.yyyy HH:mm:ss",
                    Locale.getDefault()
                ).parse(dateTimeString)

                else -> SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateTimeString)
            }

            date?.let { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(it) }
                ?: dateTimeString
        } catch (_: Exception) {
            dateTimeString
        }
    }

    /**
     * Format a date-time string from server to display time only (HH:mm)
     */
    fun formatTime(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "-"

        return try {
            val date = when {
                dateTimeString.contains('T') -> SimpleDateFormat(
                    "dd.MM.yyyy'T'HH:mm:ss",
                    Locale.getDefault()
                ).parse(dateTimeString)

                dateTimeString.contains(':') -> SimpleDateFormat(
                    "dd.MM.yyyy HH:mm:ss",
                    Locale.getDefault()
                ).parse(dateTimeString)

                else -> return dateTimeString // Can't extract time from date-only string
            }

            date?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) }
                ?: dateTimeString
        } catch (_: Exception) {
            dateTimeString
        }
    }
}

