package com.cirin0.worktimetracker.app

import android.app.Application
import com.cirin0.worktimetracker.core.localization.AppLocaleManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLocaleManager.ensureDefaultLanguage()
    }
}
