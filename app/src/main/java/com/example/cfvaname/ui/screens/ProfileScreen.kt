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
// PROFILE SCREEN
// ===================================================
@Composable
fun ProfileScreen(
    userSession: UserSession?,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(colors = listOf(GradientStart, GradientEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (userSession?.fullName?.firstOrNull()?.toString() ?: "U").uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userSession?.fullName ?: "User",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary
        )
        Text(
            text = userSession?.email ?: "",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = VenamePrimary.copy(alpha = 0.1f)
        ) {
            Text(
                text = (userSession?.role ?: "user").uppercase(),
                color = VenamePrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile info cards
        val infoItems = listOf(
            Triple(Icons.Filled.Email, "Email", userSession?.email ?: "-"),
            Triple(Icons.Filled.Badge, "Role", userSession?.role ?: "-"),
            Triple(Icons.Filled.Fingerprint, "User ID", userSession?.userId?.take(8)?.plus("...") ?: "-")
        )

        infoItems.forEach { (icon, label, value) ->
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
                    Column {
                        Text(text = label, fontSize = 12.sp, color = TextSecondary)
                        Text(text = value, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }
            }
        }
    }
}
