package com.example.profilediscoveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.profile_discovery.mvvm.viewmodel.LoginViewModel
import com.example.profilediscoveryapp.mvvm.model.database.SessionManager
import com.example.profilediscoveryapp.mvvm.model.database.UserPreferences
import com.example.profilediscoveryapp.mvvm.model.repository.AuthRepository
import com.example.profilediscoveryapp.mvvm.model.repository.ProfileRepository
import com.example.profilediscoveryapp.mvvm.view.screens.ForgotPasswordScreen
import com.example.profilediscoveryapp.mvvm.view.screens.LoginScreen
import com.example.profilediscoveryapp.mvvm.view.screens.MainScreen
import com.example.profilediscoveryapp.mvvm.view.screens.RegisterScreen
import com.example.profilediscoveryapp.mvvm.viewmodel.ForgotPasswordViewModel
import com.example.profilediscoveryapp.mvvm.viewmodel.RegisterViewModel
import com.example.profilediscoveryapp.mvvm.viewmodel.SettingsViewModel
import com.example.profilediscoveryapp.ui.theme.ProfileDiscoveryTheme


class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository
    private lateinit var profileRepository: ProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
          enableEdgeToEdge()
        // Initialize repositories and managers
        sessionManager = SessionManager(this)
        userPreferences = UserPreferences(this)
        authRepository = AuthRepository(this)
        profileRepository = ProfileRepository(this)

        setContent {
            var isDarkTheme by remember { mutableStateOf(userPreferences.isDarkModeEnabled()) }
            var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

            ProfileDiscoveryTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoggedIn) {
                        val userId = sessionManager.getUserId() ?: return@Surface
                        val settingsViewModel = remember { SettingsViewModel(userPreferences) }

                        // Observe dark mode changes
                        LaunchedEffect(settingsViewModel.isDarkModeEnabled) {
                            settingsViewModel.isDarkModeEnabled.collect { enabled ->
                                isDarkTheme = enabled
                            }
                        }

                        MainScreen(
                            userId = userId,
                            profileRepository = profileRepository,
                            settingsViewModel = settingsViewModel,
                            onLogout = {
                                authRepository.logout()
                                isLoggedIn = false
                            }
                        )
                    } else {
                        val loginViewModel = remember { LoginViewModel(authRepository) }
                        val registerViewModel = remember { RegisterViewModel(authRepository) }
                        val forgotPasswordViewModel = remember {
                            ForgotPasswordViewModel(
                                authRepository
                            )
                        }

                        var currentScreen by remember { mutableStateOf("login") }

                        when (currentScreen) {
                            "login" -> {
                                LoginScreen(
                                    viewModel = loginViewModel,
                                    onLoginSuccess = {
                                        isLoggedIn = true
                                    },
                                    onNavigateToRegister = {
                                        currentScreen = "register"
                                    },
                                    onNavigateToForgotPassword = {
                                        currentScreen = "forgot_password"
                                    }
                                )
                            }

                            "register" -> {
                                RegisterScreen(
                                    viewModel = registerViewModel,
                                    onRegisterSuccess = {
                                        currentScreen = "login"
                                    },
                                    onNavigateToLogin = {
                                        currentScreen = "login"
                                    }
                                )
                            }

                            "forgot_password" -> {
                                ForgotPasswordScreen(
                                    viewModel = forgotPasswordViewModel,
                                    onResetSuccess = {
                                        currentScreen = "login"
                                    },
                                    onNavigateToLogin = {
                                        currentScreen = "login"
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
