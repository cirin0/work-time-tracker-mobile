package com.cirin0.worktimetracker.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Chat : Screen("chat")
    object ChatDetail : Screen("chat/{userId}") {
        fun createRoute(userId: Int) = "chat/$userId"
    }

    object Profile : Screen("profile")
}