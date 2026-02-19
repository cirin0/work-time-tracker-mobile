package com.cirin0.worktimetracker.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Test : Screen("test")
    object Profile : Screen("profile")

    object Company : Screen("company/{companyId}") {
        fun createRoute(companyId: Int) = "company/$companyId"
    }

    object TimeEntries : Screen("time_entries")

    object TimeEntryDetail : Screen("time_entry/{entryId}") {
        fun createRoute(entryId: Int) = "time_entry/$entryId"
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

}