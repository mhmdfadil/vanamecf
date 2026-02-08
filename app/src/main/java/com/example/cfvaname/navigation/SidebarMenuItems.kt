package com.example.cfvaname.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

data class SidebarMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val badge: Int = 0
)

fun getSidebarMenuItems(): List<SidebarMenuItem> {
    return listOf(
        SidebarMenuItem(
            title = "Dashboard",
            icon = Icons.Filled.Dashboard,
            route = Screen.Dashboard.route
        ),
        SidebarMenuItem(
            title = "Data Hipotesis",
            icon = Icons.Filled.Biotech,
            route = Screen.Hipotesis.route
        ),
        SidebarMenuItem(
            title = "Data Gejala",
            icon = Icons.Filled.MedicalServices,
            route = Screen.Gejala.route
        ),
        SidebarMenuItem(
            title = "Nilai CF",
            icon = Icons.Filled.BarChart,
            route = Screen.NilaiCf.route
        ),
        SidebarMenuItem(
            title = "Laporan",
            icon = Icons.Filled.Assessment,
            route = Screen.Reports.route
        ),
        SidebarMenuItem(
            title = "Profil",
            icon = Icons.Filled.Person,
            route = Screen.Profile.route
        ),
        SidebarMenuItem(
            title = "Pengaturan",
            icon = Icons.Filled.Settings,
            route = Screen.Settings.route
        ),
        SidebarMenuItem(
            title = "Tentang",
            icon = Icons.Filled.Info,
            route = Screen.About.route
        )
    )
}