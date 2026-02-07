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
// ABOUT SCREEN
// ===================================================
@Composable
fun AboutScreen(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // App logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(colors = listOf(GradientStart, GradientEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Hub,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sistem Cerdas Vename",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary
        )
        Text(
            text = "Versi 1.0.0",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Tentang Aplikasi",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sistem Cerdas Vename adalah platform pengelolaan data dan monitoring " +
                            "yang dirancang untuk membantu organisasi dalam mengoptimalkan " +
                            "proses bisnis secara real-time dengan teknologi terkini.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Teknologi",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val techs = listOf(
                    "Kotlin + Jetpack Compose",
                    "Supabase (PostgreSQL + REST API)",
                    "Material Design 3",
                    "bcrypt Password Hashing"
                )
                techs.forEach { tech ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(VenamePrimary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = tech, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "© 2026 Sistem Cerdas Vename",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}