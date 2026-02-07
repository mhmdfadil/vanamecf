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
// REPORTS SCREEN
// ===================================================
@Composable
fun ReportsScreen(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Laporan",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        val reports = listOf(
            Triple("Laporan Harian", "Feb 07, 2026", Icons.Filled.CalendarToday),
            Triple("Laporan Mingguan", "Minggu ke-6", Icons.Filled.DateRange),
            Triple("Laporan Bulanan", "Februari 2026", Icons.Filled.BarChart),
            Triple("Laporan Tahunan", "2026", Icons.Filled.Assessment)
        )

        reports.forEach { (title, subtitle, icon) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VenamePrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = VenamePrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(text = subtitle, fontSize = 13.sp, color = TextSecondary)
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}
