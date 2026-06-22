@file:Suppress("unused")

package com.cirin0.worktimetracker.core.utils

import com.cirin0.worktimetracker.core.localization.AppLocaleManager
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val isoDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Output formatter for getCurrentIsoDateTime(); 'Z' is literal (UTC is enforced via withZone)
    private val isoDateTimeOutputFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
            .withZone(ZoneOffset.UTC)

    private fun currentLocale(): Locale = AppLocaleManager.getCurrentLocale()

    private fun displayDateFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy", currentLocale())
    }

    private fun displayDateTimeFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", currentLocale())
    }

    private fun displayTimeFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("HH:mm", currentLocale())
    }

    fun normalizeDayOfWeek(dayOfWeek: String): String {
        return dayOfWeek.trim().lowercase(Locale.US)
    }

    fun parseWorkTime(timeString: String?): LocalTime? {
        if (timeString.isNullOrBlank()) return null
        val normalizedTime = timeString.trim()
        return try {
            LocalTime.parse(normalizedTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        } catch (_: Exception) {
            try {
                LocalTime.parse(normalizedTime, DateTimeFormatter.ofPattern("HH:mm"))
            } catch (_: Exception) {
                null
            }
        }
    }

    @Suppress("unused")
    fun getShiftDurationMinutes(startTime: String, endTime: String): Long {
        val start = parseWorkTime(startTime) ?: return 0L
        val end = parseWorkTime(endTime) ?: return 0L
        val durationMinutes = Duration.between(start, end).toMinutes()
        return if (durationMinutes >= 0) {
            durationMinutes
        } else {
            durationMinutes + Duration.ofDays(1).toMinutes()
        }
    }

    /**
     * Parses any ISO-8601 instant string (Z suffix, with or without sub-seconds).
     * [Instant.parse] uses DateTimeFormatter.ISO_INSTANT which handles
     * "…Z", "….000Z", "….000000Z" etc. — no fallback chain needed.
     */
    private fun parseInstant(dateTimeString: String): Instant? = try {
        Instant.parse(dateTimeString)
    } catch (_: Exception) {
        null
    }

    /**
     * Parses a date string into a LocalDate.
     * Handles both simple "yyyy-MM-dd" and full ISO "yyyy-MM-ddTHH:mm:ssZ" formats.
     */
    fun parseLocalDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null
        return try {
            if (dateString.contains('T')) {
                parseInstant(dateString)
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate()
            } else {
                LocalDate.parse(dateString, isoDateFormatter)
            }
        } catch (_: Exception) {
            null
        }
    }

    // Format date: "2024-03-02" → "02.03.2024"
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "-"
        return try {
            if (dateString.contains('T')) {
                parseInstant(dateString)
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate()
                    ?.format(displayDateFormatter())
                    ?: dateString
            } else {
                LocalDate.parse(dateString, isoDateFormatter).format(displayDateFormatter())
            }
        } catch (_: Exception) {
            dateString
        }
    }

    // Format datetime: "2024-03-02T14:30:00.000000Z" → "02.03.2024 14:30"
    fun formatDateTime(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "-"
        return try {
            parseInstant(dateTimeString)
                ?.atZone(ZoneId.systemDefault())
                ?.format(displayDateTimeFormatter())
                ?: dateTimeString
        } catch (_: Exception) {
            dateTimeString
        }
    }

    // Format time: "2024-03-02T14:30:00.000000Z" → "14:30" or "09:00:00" → "09:00"
    fun formatTime(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "-"
        return try {
            if (!dateTimeString.contains('T')) {
                return dateTimeString.substring(0, minOf(5, dateTimeString.length))
            }
            parseInstant(dateTimeString)
                ?.atZone(ZoneId.systemDefault())
                ?.format(displayTimeFormatter())
                ?: dateTimeString
        } catch (_: Exception) {
            dateTimeString
        }
    }

    // Convert millis to ISO date: 1709337600000 → "2024-03-02"
    fun toIsoDate(dateMillis: Long): String {
        return Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(isoDateFormatter)
    }

    // Format hours: 8.5 → "8 год 30 хв"
    fun formatHours(hours: Double): String {
        val isEnglish = currentLocale().language == "en"
        if (hours < 0) return if (isEnglish) "0 min" else "0 хв"
        val wholeHours = hours.toInt()
        val minutes = ((hours - wholeHours) * 60).toInt()

        return when {
            wholeHours > 0 && minutes > 0 -> if (isEnglish) {
                "$wholeHours hr $minutes min"
            } else {
                "$wholeHours год $minutes хв"
            }

            wholeHours > 0 -> if (isEnglish) "$wholeHours hr" else "$wholeHours год"
            minutes > 0 -> if (isEnglish) "$minutes min" else "$minutes хв"
            else -> if (isEnglish) "0 min" else "0 хв"
        }
    }

    fun getDayDisplayName(dayOfWeek: String): String {
        return try {
            DayOfWeek.valueOf(dayOfWeek.uppercase(Locale.US))
                .getDisplayName(TextStyle.FULL, currentLocale())
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(currentLocale()) else it.toString()
                }
        } catch (_: Exception) {
            dayOfWeek.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(currentLocale()) else it.toString()
            }
        }
    }

    @Suppress("unused")
    fun getDayNameUkrainian(dayOfWeek: String): String = getDayDisplayName(dayOfWeek)

    // Day order: "monday" → 1, "sunday" → 7
    fun getDayOrder(dayOfWeek: String): Int {
        return when (normalizeDayOfWeek(dayOfWeek)) {
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

    // Get the current day of the week in English lowercase
    fun getCurrentDayOfWeek(): String {
        return normalizeDayOfWeek(LocalDate.now().dayOfWeek.name)
    }

    // Get the current date-time in ISO format
    fun getCurrentIsoDateTime(): String {
        return isoDateTimeOutputFormatter.format(Instant.now())
    }
}
