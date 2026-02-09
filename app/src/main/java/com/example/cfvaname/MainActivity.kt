package com.example.cfvaname

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cfvaname.data.PreferencesManager
import com.example.cfvaname.data.SessionManager
import com.example.cfvaname.data.UserSession
import com.example.cfvaname.navigation.Screen
import com.example.cfvaname.ui.components.AppScaffold
import com.example.cfvaname.ui.screens.*
import com.example.cfvaname.ui.theme.CFVanameTheme
import com.example.cfvaname.ui.localization.LocalLanguage
import com.example.cfvaname.ui.localization.stringResource
import com.example.cfvaname.ui.localization.AppStrings
import com.example.cfvaname.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VenameApp()
        }
    }
}

@Composable
fun VenameApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sessionManager = remember { SessionManager(context) }
    val preferencesManager = remember { PreferencesManager(context) }
    val loginViewModel: LoginViewModel = viewModel()
    val loginState by loginViewModel.uiState.collectAsState()

    // Language state
    var currentLanguage by remember { mutableStateOf(preferencesManager.getLanguage()) }
    
    // Theme state
    var currentTheme by remember { mutableStateOf(preferencesManager.getTheme()) }
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = when (currentTheme) {
        "light" -> false
        "dark" -> true
        else -> systemInDarkTheme // "system"
    }

    // Cek apakah sudah login
    var userSession by remember { mutableStateOf(sessionManager.getSession()) }
    val startDestination = if (userSession != null) Screen.Dashboard.route else Screen.Landing.route

    // Handle login success
    LaunchedEffect(loginState.loginSuccess) {
        if (loginState.loginSuccess && loginState.userSession != null) {
            sessionManager.saveSession(loginState.userSession!!)
            userSession = loginState.userSession
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
            loginViewModel.resetState()
        }
    }

    // Fungsi logout
    val handleLogout: () -> Unit = {
        sessionManager.clearSession()
        userSession = null
        loginViewModel.resetState()
        navController.navigate(Screen.Landing.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    // Fungsi navigasi
    val handleNavigate: (String) -> Unit = { route ->
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) {
                popUpTo(Screen.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    
    // Fungsi ubah bahasa
    val handleLanguageChange: (String) -> Unit = { language ->
        currentLanguage = language
        preferencesManager.setLanguage(language)
    }
    
    // Fungsi ubah tema
    val handleThemeChange: (String) -> Unit = { theme ->
        currentTheme = theme
        preferencesManager.setTheme(theme)
    }

    // Provide language dan theme
    CompositionLocalProvider(LocalLanguage provides currentLanguage) {
        CFVanameTheme(darkTheme = isDarkTheme) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                // === LANDING PAGE (sebelum login) ===
                composable(Screen.Landing.route) {
                    LandingScreen(
                        onLoginClick = {
                            navController.navigate(Screen.Login.route)
                        }
                    )
                }

                // === LOGIN PAGE ===
                composable(Screen.Login.route) {
                    LoginScreen(
                        isLoading = loginState.isLoading,
                        errorMessage = loginState.errorMessage,
                        onLogin = { email, password ->
                            loginViewModel.login(email, password)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // === DASHBOARD (setelah login, pakai AppScaffold) ===
                composable(Screen.Dashboard.route) {
                    AppScaffold(
                        currentRoute = Screen.Dashboard.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.Dashboard)
                    ) { padding ->
                        DashboardScreen(
                            userSession = userSession,
                            padding = padding
                        )
                    }
                }

                // === GEJALA (CRUD data gejala) ===
                composable(Screen.Gejala.route) {
                    AppScaffold(
                        currentRoute = Screen.Gejala.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.DataGejala)
                    ) { padding ->
                        GejalaScreen(padding = padding)
                    }
                }

                // === HIPOTESIS (CRUD data hipotesis) ===
                composable(Screen.Hipotesis.route) {
                    AppScaffold(
                        currentRoute = Screen.Hipotesis.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.DataHipotesis)
                    ) { padding ->
                        HipotesisScreen(padding = padding)
                    }
                }

                // === NILAI CF (Certainty Factor) ===
                composable(Screen.NilaiCf.route) {
                    AppScaffold(
                        currentRoute = Screen.NilaiCf.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.NilaiCf)
                    ) { padding ->
                        NilaiCfScreen(padding = padding)
                    }
                }

                // === RULES ===
                composable(Screen.Rule.route) {
                    AppScaffold(
                        currentRoute = Screen.Rule.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.Rules)
                    ) { padding ->
                        RuleScreen(padding = padding)
                    }
                }

                // === KUESIONER (Diagnosa) ===
                composable(Screen.Kuesioner.route) {
                    AppScaffold(
                        currentRoute = Screen.Kuesioner.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.Questionnaire)
                    ) { padding ->
                        KuesionerScreen(padding = padding)
                    }
                }

                // === PROFILE ===
                composable(Screen.Profile.route) {
                    AppScaffold(
                        currentRoute = Screen.Profile.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.Profile)
                    ) { padding ->
                        ProfileScreen(
                            userSession = userSession,
                            padding = padding
                        )
                    }
                }

                // === REPORTS ===
                composable(Screen.Reports.route) {
                    AppScaffold(
                        currentRoute = Screen.Reports.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.Reports)
                    ) { padding ->
                        ReportsScreen(padding = padding)
                    }
                }

                // === SETTINGS ===
                composable(Screen.Settings.route) {
                    AppScaffold(
                        currentRoute = Screen.Settings.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.Settings)
                    ) { padding ->
                        SettingsScreen(
                            padding = padding,
                            onLogout = handleLogout,
                            currentLanguage = currentLanguage,
                            onLanguageChange = handleLanguageChange,
                            currentTheme = currentTheme,
                            onThemeChange = handleThemeChange
                        )
                    }
                }

                // === ABOUT ===
                composable(Screen.About.route) {
                    AppScaffold(
                        currentRoute = Screen.About.route,
                        userSession = userSession,
                        onNavigate = handleNavigate,
                        onLogout = handleLogout,
                        title = stringResource(AppStrings.About)
                    ) { padding ->
                        AboutScreen(padding = padding)
                    }
                }
            }
        }
    }
}