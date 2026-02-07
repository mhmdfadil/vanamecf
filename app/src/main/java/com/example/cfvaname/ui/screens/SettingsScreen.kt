package com.example.cfvaname.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cfvaname.data.UserSession
import com.example.cfvaname.ui.theme.*

// ===================================================
// SETTINGS SCREEN
// ===================================================
@Composable
fun SettingsScreen(
    padding: PaddingValues,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Pengaturan",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        val settingsItems = listOf(
            Triple(Icons.Filled.Notifications, "Notifikasi", "Kelola notifikasi"),
            Triple(Icons.Filled.Language, "Bahasa", "Indonesia"),
            Triple(Icons.Filled.DarkMode, "Tema", "Mengikuti sistem"),
            Triple(Icons.Filled.Storage, "Penyimpanan", "Kelola data lokal"),
            Triple(Icons.Filled.Shield, "Privasi", "Pengaturan privasi")
        )

        settingsItems.forEach { (icon, title, subtitle) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = VenamePrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(text = subtitle, fontSize = 13.sp, color = TextSecondary)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout button
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = StatusError),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar dari Akun", fontWeight = FontWeight.Bold)
        }
    }
}