package com.cirin0.worktimetracker.core.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@SuppressLint("ConstantLocale")
object DateUtils {
    private val isoDateTimeFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    private val isoDateTimeFormatShort =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val displayDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val displayDateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    private val displayTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    /**
     * Format a date string from server (ISO 8601: "2026-03-09") to display date (dd.MM.yyyy)
     */
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "-"

        return try {
            val date = isoDateFormat.parse(dateString)
            date?.let { displayDateFormat.format(it) } ?: dateString
        } catch (_: Exception) {
            try {
                val date = isoDateTimeFormat.parse(dateString)
                date?.let { displayDateFormat.format(it) } ?: dateString
            } catch (_: Exception) {
                dateString
            }
        }
    }

    /**
     * Format a date-time string from server (ISO 8601: "2026-02-17T21:47:48.000000Z")
     * to display date-time (dd.MM.yyyy HH:mm:ss)
     */
    fun formatDateTime(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "-"

        return try {
            val date = isoDateTimeFormat.parse(dateTimeString)
            date?.let { displayDateTimeFormat.format(it) } ?: dateTimeString
        } catch (_: Exception) {
            try {
                val date = isoDateTimeFormatShort.parse(dateTimeString)
                date?.let { displayDateTimeFormat.format(it) } ?: dateTimeString
            } catch (_: Exception) {
                dateTimeString
            }
        }
    }

    /**
     * Format a date-time string from server to display time only (HH:mm)
     * Handles both ISO 8601 timestamps and time-only strings ("09:00")
     */
    fun formatTime(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "-"

        return try {
            if (!dateTimeString.contains('T') && dateTimeString.length <= 8) {
                return dateTimeString.substring(0, minOf(5, dateTimeString.length))
            }

            val date = try {
                isoDateTimeFormat.parse(dateTimeString)
            } catch (_: Exception) {
                isoDateTimeFormatShort.parse(dateTimeString)
            }

            date?.let { displayTimeFormat.format(it) } ?: dateTimeString
        } catch (_: Exception) {
            dateTimeString
        }
    }

    /**
     * Convert date to ISO 8601 format for API submission (yyyy-MM-dd)
     */
    fun toIsoDate(dateMillis: Long): String {
        return isoDateFormat.format(dateMillis)
    }
}

