package com.cirin0.worktimetracker.core.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val ukrainianLocale = Locale.Builder().setLanguage("uk").setRegion("UA").build()

    private val isoDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", ukrainianLocale)
    private val displayDateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", ukrainianLocale)
    private val displayTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Output formatter for getCurrentIsoDateTime(); 'Z' is literal (UTC is enforced via withZone)
    private val isoDateTimeOutputFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
            .withZone(ZoneOffset.UTC)

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

    // Format date: "2024-03-02" → "02.03.2024"
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "-"
        return try {
            if (dateString.contains('T')) {
                parseInstant(dateString)
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate()
                    ?.format(displayDateFormatter)
                    ?: dateString
            } else {
                LocalDate.parse(dateString, isoDateFormatter).format(displayDateFormatter)
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
                ?.format(displayDateTimeFormatter)
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
                ?.format(displayTimeFormatter)
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

    // Get current day of week in English lowercase
    fun getCurrentDayOfWeek(): String {
        return LocalDate.now().dayOfWeek.name.lowercase()
    }

    // Get the current date-time in ISO format
    fun getCurrentIsoDateTime(): String {
        return isoDateTimeOutputFormatter.format(Instant.now())
    }
}
