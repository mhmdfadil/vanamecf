package com.example.cfvaname.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cfvaname.data.UserSession
import com.example.cfvaname.ui.theme.*

@Composable
fun DashboardScreen(
    userSession: UserSession?,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(VenamePrimary, VenameAccent)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Halo, ${userSession?.fullName ?: "User"} 👋",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Selamat datang di Sistem Cerdas Vename",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.People,
                value = "1,234",
                label = "Total Pengguna",
                color = VenamePrimary
            )
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.TrendingUp,
                value = "89%",
                label = "Performa",
                color = StatusSuccess
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Storage,
                value = "45 GB",
                label = "Data Terkelola",
                color = VenameAccent
            )
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CheckCircle,
                value = "99.9%",
                label = "Uptime",
                color = StatusWarning
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity
        Text(
            text = "Aktivitas Terbaru",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        val activities = listOf(
            Triple(Icons.Filled.PersonAdd, "User baru terdaftar", "2 menit lalu"),
            Triple(Icons.Filled.CloudUpload, "Data berhasil di-upload", "15 menit lalu"),
            Triple(Icons.Filled.Update, "Sistem telah diperbarui", "1 jam lalu"),
            Triple(Icons.Filled.Analytics, "Laporan bulanan dibuat", "3 jam lalu"),
            Triple(Icons.Filled.Security, "Backup otomatis selesai", "6 jam lalu")
        )

        activities.forEach { (icon, title, time) ->
            ActivityItem(icon = icon, title = title, time = time)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DashboardStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ActivityItem(
    icon: ImageVector,
    title: String,
    time: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VenamePrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VenamePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}