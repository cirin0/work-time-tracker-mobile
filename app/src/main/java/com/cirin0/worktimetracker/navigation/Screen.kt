package com.cirin0.worktimetracker.navigation

import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Register : Screen("register")

    object VerifyEmail : Screen("verify_email/{email}") {
        fun createRoute(email: String) = "verify_email/${Uri.encode(email)}"
    }

    object Main : Screen("main")
    object Settings : Screen("settings")
    object Profile : Screen("profile")

    object Company : Screen("company") {
        fun createRoute() = "company"
    }

    object TimeEntries : Screen("time_entries")

    object TimeEntryDetail : Screen("time_entry/{entryId}") {
        fun createRoute(entryId: Int) = "time_entry/$entryId"
    }

    object Schedule : Screen("schedule")

    object Timesheet : Screen("timesheet")

    object LeaveRequests : Screen("leave_requests")

    object LeaveRequestDetail : Screen("leave_request/{requestId}") {
        fun createRoute(requestId: Int) = "leave_request/$requestId"
    }

    object ChatList : Screen("chat_list")

    object Chat : Screen("chat/{receiverId}/{receiverName}/{receiverAvatar}") {
        fun createRoute(receiverId: Int, receiverName: String, receiverAvatar: String?) =
            "chat/$receiverId/$receiverName/${
                receiverAvatar?.let {
                    URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                } ?: "null"
            }"
    }

    object Manager : Screen("manager")
}