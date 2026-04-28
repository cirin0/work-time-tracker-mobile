package com.cirin0.worktimetracker.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cirin0.worktimetracker.features.auth.presentation.login.LoginScreen
import com.cirin0.worktimetracker.features.auth.presentation.register.RegisterScreen
import com.cirin0.worktimetracker.features.auth.presentation.verifyemail.VerifyEmailScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = false
                        }
                    }
                },
                onNavigateToVerification = { email: String ->
                    navController.navigate(Screen.VerifyEmail.createRoute(email)) {
                        popUpTo(Screen.Login.route) {
                            inclusive = false
                        }
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) {
                            inclusive = false
                        }
                    }
                },
                onRegisterSuccess = { email: String, password: String ->
                    navController.navigate(Screen.VerifyEmail.createRoute(email, password)) {
                        popUpTo(Screen.Register.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(
            route = Screen.VerifyEmail.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("password") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            VerifyEmailScreen(
                email = email,
                onVerificationSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.VerifyEmail.route) {
                            inclusive = true
                        }
                    }
                },
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.VerifyEmail.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            val bottomNavController = rememberNavController()
            MainScaffold(
                navController = bottomNavController,
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}