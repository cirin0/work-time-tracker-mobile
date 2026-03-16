package com.cirin0.worktimetracker.core.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object AppLocaleManager {
    const val DEFAULT_LANGUAGE = "uk"
    const val ENGLISH_LANGUAGE = "en"

    private val supportedLanguages = setOf(DEFAULT_LANGUAGE, ENGLISH_LANGUAGE)

    fun normalizeLanguage(language: String?): String {
        return language?.takeIf { it in supportedLanguages } ?: DEFAULT_LANGUAGE
    }

    fun getCurrentLanguage(): String {
        val currentLanguage = AppCompatDelegate.getApplicationLocales().get(0)?.language
        return normalizeLanguage(currentLanguage)
    }

    fun getCurrentLocale(): Locale {
        return AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
    }

    fun applyAppLanguage(language: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(normalizeLanguage(language))
        )
    }

    fun ensureDefaultLanguage() {
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags().isBlank()) {
            applyAppLanguage(DEFAULT_LANGUAGE)
        }
    }
}

