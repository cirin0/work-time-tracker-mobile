package com.cirin0.worktimetracker.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Test : Screen("test")
    object Profile : Screen("profile")
    object Chat : Screen("chat")
    object ChatDetail : Screen("chat/{userId}") {
        fun createRoute(userId: Int) = "chat/$userId"
    }

    object Company : Screen("company/{companyId}") {
        fun createRoute(companyId: Int) = "company/$companyId"
    }
}