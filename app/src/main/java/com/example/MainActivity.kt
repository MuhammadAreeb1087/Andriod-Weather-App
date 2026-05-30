package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WeatherViewModel = viewModel()
            val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val activeUser by viewModel.activeSession.collectAsStateWithLifecycle()

            // State router navigation
            var currentScreen by remember { mutableStateOf("auth") }
            var targetChatCity by remember { mutableStateOf("Karachi") }
            var targetChatTemp by remember { mutableStateOf(26.0) }
            
            var targetTravelCity by remember { mutableStateOf("Karachi") }
            var targetTravelTemp by remember { mutableStateOf(26.0) }
            var targetTravelCode by remember { mutableStateOf(0) }

            // Automatic redirection based on logged-in state
            LaunchedEffect(activeUser) {
                if (activeUser != null) {
                    if (currentScreen == "auth") {
                        currentScreen = "dashboard"
                    }
                } else {
                    currentScreen = "auth"
                }
            }

            // High-priority Push Notifications permissions prompt for Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            MyApplicationTheme(darkTheme = isDark) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BoxWithConstraints(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_router"
                        ) { screen ->
                            when (screen) {
                                "auth" -> {
                                    AuthScreen(
                                        onAuthSuccess = {
                                            currentScreen = "dashboard"
                                        },
                                        onGoogleLoginRequested = { email, name ->
                                            viewModel.loginWithGoogle(email, name)
                                        },
                                        onEmailLoginRequested = { email, pass, onRes ->
                                            viewModel.login(email, pass, onRes)
                                        },
                                        onEmailRegisterRequested = { email, name, pass, onRes ->
                                            viewModel.registerUser(email, name, pass, onRes)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                "dashboard" -> {
                                    WeatherDashboardScreen(
                                        userName = activeUser?.fullName ?: "Pak Guest",
                                        isDarkMode = isDark,
                                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                                        onOpenChat = { city, temp ->
                                            targetChatCity = city
                                            targetChatTemp = temp
                                            currentScreen = "chatbot"
                                        },
                                        onOpenTravelInsights = { city, temp, code ->
                                            targetTravelCity = city
                                            targetTravelTemp = temp
                                            targetTravelCode = code
                                            currentScreen = "travel"
                                        },
                                        onLogout = {
                                            viewModel.logout()
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                "chatbot" -> {
                                    ChatbotScreen(
                                        currentCity = targetChatCity,
                                        currentTemperature = targetChatTemp,
                                        onBack = { currentScreen = "dashboard" },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                "travel" -> {
                                    TravelSuggestionsScreen(
                                        city = targetTravelCity,
                                        temperature = targetTravelTemp,
                                        weatherCode = targetTravelCode,
                                        onBack = { currentScreen = "dashboard" },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
