package com.cirin0.worktimetracker.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.features.company.presentation.CompanyScreen
import com.cirin0.worktimetracker.features.home.ConnectivityViewModel
import com.cirin0.worktimetracker.features.home.MainScreen
import com.cirin0.worktimetracker.features.leaverequests.presentation.LeaveRequestsScreen
import com.cirin0.worktimetracker.features.message.presentation.ChatListScreen
import com.cirin0.worktimetracker.features.message.presentation.ChatScreen
import com.cirin0.worktimetracker.features.profile.presentation.ProfileScreen
import com.cirin0.worktimetracker.features.settings.SettingsScreen
import com.cirin0.worktimetracker.features.timeentries.presentation.TimeEntryDetailScreen
import com.cirin0.worktimetracker.features.timesheet.presentation.TimesheetScreen
import com.cirin0.worktimetracker.features.workschedule.presentation.ScheduleScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
            icon = Icons.Default.AccessTime,
            label = stringResource(R.string.nav_time)
        ),
        BottomNavItem(
            route = Screen.Schedule.route,
            icon = Icons.Default.DateRange,
            label = stringResource(R.string.nav_schedule)
        ),
        BottomNavItem(
            route = Screen.Timesheet.route,
            icon = Icons.Default.Assessment,
            label = stringResource(R.string.nav_timesheet)
        ),
        BottomNavItem(
            route = Screen.Profile.route,
            icon = Icons.Default.Person,
            label = stringResource(R.string.nav_profile)
        )
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val shouldShowBottomBar = currentDestination?.route?.startsWith("chat/") != true

            if (shouldShowBottomBar) {
                NavigationBar {
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
                                if (item.route == Screen.Profile.route) {
                                    val isOnNestedProfileScreen =
                                        currentDestination?.route in listOf(
                                            Screen.LeaveRequests.route,
                                            Screen.LeaveRequestDetail.route,
                                            Screen.Company.route,
                                            Screen.Settings.route,
                                            Screen.ChatList.route
                                        ) || currentDestination?.route?.startsWith("chat/") == true

                                    if (isOnNestedProfileScreen) {
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Profile.route) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                } else {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
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
                            contentDescription = stringResource(R.string.general_offline),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = stringResource(R.string.general_no_internet),
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
                    MainScreen(
                        onNavigateToDetail = { entryId ->
                            navController.navigate(Screen.TimeEntryDetail.createRoute(entryId))
                        }
                    )
                }
                composable(Screen.Schedule.route) {
                    ScheduleScreen()
                }
                composable(Screen.Timesheet.route) {
                    TimesheetScreen()
                }
                composable(
                    route = Screen.TimeEntryDetail.route,
                    arguments = listOf(navArgument("entryId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getInt("entryId") ?: 0
                    TimeEntryDetailScreen(
                        timeEntryId = entryId,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onLogoutSuccess = onLogoutSuccess
                    )
                }
                composable(Screen.ChatList.route) {
                    ChatListScreen(
                        onNavigateToChat = { receiverId, receiverName, receiverAvatar ->
                            navController.navigate(
                                Screen.Chat.createRoute(
                                    receiverId,
                                    receiverName,
                                    receiverAvatar
                                )
                            )
                        }
                    )
                }
                composable(
                    route = Screen.Chat.route,
                    arguments = listOf(
                        navArgument("receiverId") { type = NavType.IntType },
                        navArgument("receiverName") { type = NavType.StringType },
                        navArgument("receiverAvatar") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val receiverId = backStackEntry.arguments?.getInt("receiverId") ?: 0
                    val receiverName = backStackEntry.arguments?.getString("receiverName") ?: ""
                    val receiverAvatar = backStackEntry.arguments?.getString("receiverAvatar")
                        ?.takeIf { it != "null" }
                        ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
                    ChatScreen(
                        receiverId = receiverId,
                        receiverName = receiverName,
                        receiverAvatar = receiverAvatar,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateToCompany = { companyId ->
                            navController.navigate(Screen.Company.createRoute(companyId))
                        },
                        onNavigateToChat = {
                            navController.navigate(Screen.ChatList.route)
                        },
                        onNavigateToRequests = {
                            navController.navigate(Screen.LeaveRequests.route)
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onNavigateToSchedule = {
                            navController.navigate(Screen.Schedule.route)
                        }
                    )
                }
                composable(Screen.LeaveRequests.route) {
                    LeaveRequestsScreen(
                        onNavigateToDetail = { requestId ->
                            navController.navigate(Screen.LeaveRequestDetail.createRoute(requestId))
                        },
						onNavigateBack = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }
                composable(
                    route = Screen.LeaveRequestDetail.route,
                    arguments = listOf(navArgument("requestId") { type = NavType.IntType })
                ) {
                    com.cirin0.worktimetracker.features.leaverequests.presentation.detail.LeaveRequestDetailScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = Screen.Company.route,
                    arguments = listOf(navArgument("companyId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val companyId = backStackEntry.arguments?.getInt("companyId") ?: 0
                    CompanyScreen(
                        companyId = companyId,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
