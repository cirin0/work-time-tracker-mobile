package com.cirin0.worktimetracker.core.utils

object Constants {
    const val DATABASE_NAME = "app_database"
    const val PREFS_NAME = "app_prefs"
    const val NAMED_IMAGE_URL = "image_base_url"

    object Validation {
        const val MIN_PASSWORD_LENGTH = 8
        const val MIN_COMMENT_LENGTH = 3
        const val MAX_COMMENT_LENGTH = 500
        const val PIN_CODE_LENGTH = 4
        const val MIN_NAME_LENGTH = 2
        const val MAX_NAME_LENGTH = 50
    }

    object ApiRoutes {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val VERIFY_EMAIL = "auth/verify-email"
        const val RESEND_VERIFICATION_CODE = "auth/resend-verification-code"
        const val REFRESH = "auth/refresh"
        const val LOGOUT = "auth/logout"
        const val ME = "me"
        const val ME_WORK_SCHEDULE = "me/work-schedule"
        const val UPDATE_PROFILE = "me"
        const val UPDATE_AVATAR = "me/avatar"
        const val SETUP_PIN_CODE = "me/pin-code"
        const val UPDATE_PIN_CODE = "me/pin-code"
        const val FCM_TOKEN = "me/fcm-token"
        const val USERS = "users"
        const val COMPANIES = "companies"
        const val TIME_ENTRIES = "time-entries"
        const val LEAVE_REQUESTS = "leave-requests"
        const val MESSAGES = "messages"
        const val MESSAGES_BY_RECEIVER = "messages/{receiverId}"
    }

    object Ably {
        const val PUBLIC_KEY = "-_Zznw.SbvBHA"
    }
}