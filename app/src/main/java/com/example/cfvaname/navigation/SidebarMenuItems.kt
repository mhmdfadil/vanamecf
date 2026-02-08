package com.example.cfvaname.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cfvaname.ui.localization.AppStrings
import com.example.cfvaname.ui.localization.stringResource

data class SidebarMenuItem(
    val titleRes: AppStrings, // Menggunakan AppStrings untuk multi-bahasa
    val icon: ImageVector,
    val iconOutlined: ImageVector,
    val route: String,
    val badge: Int = 0
)

/**
 * Mendapatkan semua item menu sidebar
 * Note: Title akan diambil dari stringResource() saat render
 */
fun getSidebarMenuItems(): List<SidebarMenuItem> {
    return listOf(
        SidebarMenuItem(
            titleRes = AppStrings.Dashboard,
            icon = Icons.Filled.Dashboard,
            iconOutlined = Icons.Outlined.Dashboard,
            route = Screen.Dashboard.route
        ),
        SidebarMenuItem(
            titleRes = AppStrings.Hipotesis,
            icon = Icons.Filled.Biotech,
            iconOutlined = Icons.Outlined.Biotech,
            route = Screen.Hipotesis.route
        ),
        SidebarMenuItem(
            titleRes = AppStrings.Gejala,
            icon = Icons.Filled.MedicalServices,
            iconOutlined = Icons.Outlined.MedicalServices,
            route = Screen.Gejala.route
        ),
        SidebarMenuItem(
            titleRes = AppStrings.NilaiCf,
            icon = Icons.Filled.BarChart,
            iconOutlined = Icons.Outlined.BarChart,
            route = Screen.NilaiCf.route
        ),
        SidebarMenuItem(
            titleRes = AppStrings.Reports,
            icon = Icons.Filled.Assessment,
            iconOutlined = Icons.Outlined.Assessment,
            route = Screen.Reports.route
        ),
        SidebarMenuItem(
            titleRes = AppStrings.Profile,
            icon = Icons.Filled.Person,
            iconOutlined = Icons.Outlined.Person,
            route = Screen.Profile.route
        ),
        SidebarMenuItem(
            titleRes = AppStrings.Settings,
            icon = Icons.Filled.Settings,
            iconOutlined = Icons.Outlined.Settings,
            route = Screen.Settings.route
        ),
        SidebarMenuItem(
            titleRes = AppStrings.About,
            icon = Icons.Filled.Info,
            iconOutlined = Icons.Outlined.Info,
            route = Screen.About.route
        )
    )
}