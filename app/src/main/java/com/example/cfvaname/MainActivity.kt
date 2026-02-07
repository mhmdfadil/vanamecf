package com.example.cfvaname

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cfvaname.data.SessionManager
import com.example.cfvaname.data.UserSession
import com.example.cfvaname.navigation.Screen
import com.example.cfvaname.ui.components.AppScaffold
import com.example.cfvaname.ui.screens.*
import com.example.cfvaname.ui.theme.CFVanameTheme
import com.example.cfvaname.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CFVanameTheme {
                VenameApp()
            }
        }
    }
}

@Composable
fun VenameApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sessionManager = remember { SessionManager(context) }
    val loginViewModel: LoginViewModel = viewModel()
    val loginState by loginViewModel.uiState.collectAsState()

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
                title = "Dashboard"
            ) { padding ->
                DashboardScreen(
                    userSession = userSession,
                    padding = padding
                )
            }
        }

        // === PROFILE ===
        composable(Screen.Profile.route) {
            AppScaffold(
                currentRoute = Screen.Profile.route,
                userSession = userSession,
                onNavigate = handleNavigate,
                onLogout = handleLogout,
                title = "Profil"
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
                title = "Laporan"
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
                title = "Pengaturan"
            ) { padding ->
                SettingsScreen(
                    padding = padding,
                    onLogout = handleLogout
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
                title = "Tentang"
            ) { padding ->
                AboutScreen(padding = padding)
            }
        }
    }
}