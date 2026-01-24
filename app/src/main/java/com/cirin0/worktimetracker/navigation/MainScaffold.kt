package com.cirin0.worktimetracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cirin0.worktimetracker.features.home.MainScreen
import com.cirin0.worktimetracker.features.profile.presentation.ProfileScreen
import com.cirin0.worktimetracker.features.test.TestScreen

@Composable
fun MainScaffold(
    navController: NavHostController,
    onLogoutSuccess: () -> Unit = {}
) {
    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.Main.route,
            icon = Icons.Default.Home,
            label = "Головна"
        ),
        BottomNavItem(
            route = Screen.Test.route,
            icon = Icons.Default.Quiz,
            label = "Тест"
        ),
        BottomNavItem(
            route = Screen.Profile.route,
            icon = Icons.Default.Person,
            label = "Профіль"
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    val isSelected =
                        currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            if (isSelected) {
                                Text(item.label)
                            }
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Main.route) {
                MainScreen()
            }
            composable(Screen.Test.route) {
                TestScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogoutSuccess = onLogoutSuccess
                )
            }
        }
    }
}
