package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.theme.CollegeERPTheme
import com.example.ui.viewmodel.ERPViewModel
import com.example.ui.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ERPViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val sessionManager = viewModel.sessionManager
            // Read dark mode state from session preference
            var darkThemeEnabled by remember { mutableStateOf(sessionManager.isDarkMode) }

            // Periodically keep theme in sync
            LaunchedEffect(sessionManager.isDarkMode) {
                darkThemeEnabled = sessionManager.isDarkMode
            }

            CollegeERPTheme(darkTheme = darkThemeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var currentRoute by remember {
                        mutableStateOf(
                            if (sessionManager.isLoggedIn) {
                                if (sessionManager.role == "Admin") "admin_dashboard" else "student_dashboard"
                            } else {
                                "login"
                            }
                        )
                    }

                    var previousRouteBeforeSettings by remember { mutableStateOf("login") }

                    Crossfade(targetState = currentRoute, label = "NavigationCrossfade") { route ->
                        when (route) {
                            "login" -> {
                                LoginScreen(
                                    viewModel = loginViewModel,
                                    onLoginSuccess = { role ->
                                        viewModel.refreshAllData()
                                        currentRoute = if (role == "Admin") "admin_dashboard" else "student_dashboard"
                                    },
                                    onConfigureUrl = {
                                        previousRouteBeforeSettings = "login"
                                        currentRoute = "settings"
                                    }
                                )
                            }
                            "admin_dashboard" -> {
                                AdminDashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = {
                                        previousRouteBeforeSettings = "admin_dashboard"
                                        currentRoute = "settings"
                                    },
                                    onLogout = {
                                        sessionManager.logout()
                                        currentRoute = "login"
                                    }
                                )
                            }
                            "student_dashboard" -> {
                                StudentDashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = {
                                        previousRouteBeforeSettings = "student_dashboard"
                                        currentRoute = "settings"
                                    },
                                    onLogout = {
                                        sessionManager.logout()
                                        currentRoute = "login"
                                    }
                                )
                            }
                            "settings" -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        currentRoute = previousRouteBeforeSettings
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
