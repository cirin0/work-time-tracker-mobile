package com.cirin0.worktimetracker.core.utils

object Constants {
    const val DATABASE_NAME = "app_database"
    const val PREFS_NAME = "app_prefs"

    object ApiRoutes {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val REFRESH = "auth/refresh"
        const val LOGOUT = "auth/logout"
        const val ME = "me"
        const val USERS = "users"
        const val MESSAGES = "messages/{userId}"
    }
}