package com.cirin0.worktimetracker.core.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@SuppressLint("ConstantLocale")
object DateUtils {
    private val ukrainianLocale = Locale.Builder().setLanguage("uk").setRegion("UA").build()

    // ISO formatters (server format)
    private val isoDateTimeFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val isoDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    private val displayDateFormat by lazy {
        SimpleDateFormat("dd.MM.yyyy", ukrainianLocale)
    }

    private val displayDateTimeFormat by lazy {
        SimpleDateFormat("dd.MM.yyyy HH:mm", ukrainianLocale)
    }

    private val displayTimeFormat by lazy {
        SimpleDateFormat("HH:mm", ukrainianLocale)
    }

    private fun parseIsoDateTime(dateTimeString: String): Date? {
        return try {
            isoDateTimeFormat.parse(dateTimeString)
        } catch (_: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(dateTimeString)
            } catch (_: Exception) {
                try {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.parse(dateTimeString)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    // Format date: "2024-03-02" → "02.03.2024"
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "-"
        return try {
            val date = isoDateFormat.parse(dateString) ?: parseIsoDateTime(dateString)
            date?.let { displayDateFormat.format(it) } ?: dateString
        } catch (_: Exception) {
            dateString
        }
    }

    // Format datetime: "2024-03-02T14:30:00.000000Z" → "02.03.2024 14:30"
    fun formatDateTime(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "-"
        return try {
            val date = parseIsoDateTime(dateTimeString)
            date?.let { displayDateTimeFormat.format(it) } ?: dateTimeString
        } catch (_: Exception) {
            dateTimeString
        }
    }

    // Format time: "2024-03-02T14:30:00.000000Z" → "14:30" or "09:00" → "09:00"
    fun formatTime(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "-"
        return try {
            if (!dateTimeString.contains('T')) {
                return dateTimeString.substring(0, minOf(5, dateTimeString.length))
            }
            val date = parseIsoDateTime(dateTimeString)
            date?.let { displayTimeFormat.format(it) } ?: dateTimeString
        } catch (_: Exception) {
            dateTimeString
        }
    }

    // Convert millis to ISO date: 1709337600000 → "2024-03-02"
    fun toIsoDate(dateMillis: Long): String {
        return isoDateFormat.format(Date(dateMillis))
    }

    // Format hours: 8.5 → "8 год 30 хв"
    fun formatHours(hours: Double): String {
        if (hours < 0) return "0 хв"
        val wholeHours = hours.toInt()
        val minutes = ((hours - wholeHours) * 60).toInt()
        return when {
            wholeHours > 0 && minutes > 0 -> "$wholeHours год $minutes хв"
            wholeHours > 0 -> "$wholeHours год"
            minutes > 0 -> "$minutes хв"
            else -> "0 хв"
        }
    }

    // Day of the week: "monday" → "Понеділок"
    fun getDayNameUkrainian(dayOfWeek: String): String {
        return when (dayOfWeek.lowercase()) {
            "monday" -> "Понеділок"
            "tuesday" -> "Вівторок"
            "wednesday" -> "Середа"
            "thursday" -> "Четвер"
            "friday" -> "П'ятниця"
            "saturday" -> "Субота"
            "sunday" -> "Неділя"
            else -> dayOfWeek.replaceFirstChar { it.uppercase() }
        }
    }

    // Day order: "monday" → 1, "sunday" → 7
    fun getDayOrder(dayOfWeek: String): Int {
        return when (dayOfWeek.lowercase()) {
            "monday" -> 1
            "tuesday" -> 2
            "wednesday" -> 3
            "thursday" -> 4
            "friday" -> 5
            "saturday" -> 6
            "sunday" -> 7
            else -> 8
        }
    }

    // Get the current date-time in ISO format
    fun getCurrentIsoDateTime(): String {
        return isoDateTimeFormat.format(Date())
    }
}


