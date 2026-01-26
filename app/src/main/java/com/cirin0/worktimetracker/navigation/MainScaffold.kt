package com.cirin0.worktimetracker.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cirin0.worktimetracker.features.home.ConnectivityViewModel
import com.cirin0.worktimetracker.features.home.MainScreen
import com.cirin0.worktimetracker.features.profile.presentation.ProfileScreen
import com.cirin0.worktimetracker.features.test.TestScreen

@Composable
fun MainScaffold(
    navController: NavHostController,
    onLogoutSuccess: () -> Unit = {},
    connectivityViewModel: ConnectivityViewModel = hiltViewModel()
) {
    val isOffline by connectivityViewModel.isOffline.collectAsState()

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
        Column(modifier = Modifier.padding(innerPadding)) {
            if (isOffline) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Офлайн",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "Немає підключення до інтернету",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            NavHost(
                navController = navController,
                startDestination = Screen.Main.route
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
}
