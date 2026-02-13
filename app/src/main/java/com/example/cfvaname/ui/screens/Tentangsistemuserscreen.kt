package com.example.cfvaname.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.ui.localization.*

@Composable
fun TentangSistemUserScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VenameBgLight)
            .verticalScroll(rememberScrollState())
    ) {
        // === HEADER ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(VenamePrimaryDark, VenamePrimary)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 32.dp)
        ) {
            Column {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Tentang Sistem",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sistem Pakar Diagnosis Penyakit Udang Vaname",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === ABOUT APPLICATION ===
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        tint = VenamePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tentang Aplikasi",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sistem Pakar Diagnosis Penyakit Udang Vaname adalah aplikasi berbasis Android yang dirancang untuk membantu petambak udang dalam mendiagnosis penyakit pada udang vaname secara cepat dan akurat. Sistem ini menggunakan metode Certainty Factor (CF) untuk menghitung tingkat keyakinan diagnosis berdasarkan gejala yang dialami udang.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // === CERTAINTY FACTOR METHOD ===
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Functions,
                        contentDescription = null,
                        tint = VenameAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Metode Certainty Factor",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Certainty Factor (CF) adalah metode untuk mengukur tingkat kepastian terhadap suatu fakta atau aturan. Metode ini cocok digunakan dalam sistem pakar karena dapat menangani ketidakpastian dalam data gejala penyakit.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Formula cards
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = VenamePrimary.copy(alpha = 0.06f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Rumus Perhitungan:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = VenamePrimaryDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "CF(gejala) = CF(pakar) × CF(user)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = VenamePrimary,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CF(combine) = CF(old) + CF(new) × (1 - CF(old))",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = VenamePrimary,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CF(persentase) = CF(combine) × 100%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = VenamePrimary,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // === FEATURES ===
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = VenameSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fitur Utama",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                val features = listOf(
                    "Diagnosis Penyakit" to "Mendiagnosis 13 jenis penyakit udang vaname berdasarkan gejala",
                    "Perhitungan CF" to "Menghitung tingkat keyakinan diagnosis menggunakan metode Certainty Factor",
                    "Hasil Detail" to "Menampilkan hasil diagnosis lengkap dengan persentase keyakinan",
                    "Ekspor PDF" to "Mengekspor hasil diagnosis ke format PDF untuk dokumentasi",
                    "Riwayat Diagnosis" to "Menyimpan riwayat diagnosis yang pernah dilakukan"
                )

                features.forEach { (title, desc) ->
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .offset(y = 6.dp)
                                .clip(CircleShape)
                                .background(VenamePrimary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // === DISEASE COVERAGE ===
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.BugReport,
                        contentDescription = null,
                        tint = StatusError,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cakupan Penyakit",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sistem ini dapat mendiagnosis 13 jenis penyakit utama pada udang vaname:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val diseases = listOf(
                    "TSV - Taura Syndrome Virus",
                    "CMNV - Covert Mortality Nodavirus",
                    "YHV - Yellow Head Virus",
                    "WFD - White Feces Disease",
                    "WSSV - White Spot Syndrome Virus",
                    "IMNV - Infectious Myonecrosis Virus",
                    "IHHNV - Infectious Hypodermal and Hematopoietic Necrosis Virus",
                    "EMS - Early Mortality Syndrome",
                    "CSS - Chronic Softshell Syndrome",
                    "BC - Body Cramp",
                    "Black Gill Disease",
                    "EHP - Enterocytozoon Hepatopenaei",
                    "Vibriosis"
                )

                diseases.chunked(2).forEach { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        chunk.forEach { disease ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = disease,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (chunk.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Text(
            text = "© 2026 Sistem Cerdas Vaname",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}