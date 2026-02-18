package com.cirin0.worktimetracker.core.utils

object Constants {
    const val DATABASE_NAME = "app_database"
    const val PREFS_NAME = "app_prefs"
    const val NAMED_IMAGE_URL = "image_base_url"

    object ApiRoutes {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val REFRESH = "auth/refresh"
        const val LOGOUT = "auth/logout"
        const val ME = "me"
        const val UPDATE_PROFILE = "me"
        const val UPDATE_AVATAR = "me/avatar"
        const val SETUP_PIN_CODE = "me/pin-code"
        const val UPDATE_PIN_CODE = "me/pin-code"
        const val USERS = "users"
        const val COMPANIES = "companies"
        const val TIME_ENTRIES = "time-entries"
        const val MESSAGES = "messages"
        const val MESSAGES_BY_RECEIVER = "messages/{receiverId}"
    }

    object Reverb {
        const val HOST = "192.168.0.52"
        const val PORT = 8080
        const val APP_KEY = "9smphaclcxahdff41mik"
        const val CLUSTER = "eu"
        const val USE_TLS = false
    }
}