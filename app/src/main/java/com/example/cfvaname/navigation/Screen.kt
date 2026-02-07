package com.example.cfvaname.navigation

/**
 * Definisi semua route navigasi di aplikasi
 */
sealed class Screen(val route: String) {
    data object Landing : Screen("landing")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Reports : Screen("reports")
    data object About : Screen("about")
}