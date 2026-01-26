package com.cirin0.worktimetracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.cirin0.worktimetracker.core.utils.ConnectivityObserver
import com.cirin0.worktimetracker.features.auth.data.repository.AuthRepository
import com.cirin0.worktimetracker.features.home.ThemeViewModel
import com.cirin0.worktimetracker.navigation.NavGraph
import com.cirin0.worktimetracker.navigation.Screen
import com.cirin0.worktimetracker.ui.theme.WorkTimeTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            connectivityObserver.observe().collect { status ->
                when (status) {
                    ConnectivityObserver.Status.Unavailable,
                    ConnectivityObserver.Status.Lost -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Немає підключення до інтернету",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    ConnectivityObserver.Status.Available -> {
                        // show "Back online" message
                    }

                    else -> {}
                }
            }
        }

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val useDarkTheme by themeViewModel.userThemePreference.collectAsState()

            WorkTimeTrackerTheme(darkTheme = useDarkTheme ?: isSystemInDarkTheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isAuthenticated by authRepository.isAuthenticated.collectAsState(initial = null)
                    when (isAuthenticated) {
                        null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        true -> {
                            NavGraph(
                                navController = navController,
                                startDestination = Screen.Main.route
                            )
                        }

                        false -> {
                            NavGraph(
                                navController = navController,
                                startDestination = Screen.Login.route
                            )
                        }
                    }
                }
            }
        }
    }
}
