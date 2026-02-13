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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cfvaname.ui.theme.*

@Composable
fun CaraPakaiUserScreen(
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
                            Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Cara Menggunakan",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panduan Lengkap Sistem",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === INTRO ===
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            color = VenamePrimary.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = VenamePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ikuti langkah-langkah berikut untuk melakukan diagnosis penyakit udang vaname dengan mudah dan akurat.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === STEP BY STEP ===
        val steps = listOf(
            StepData(
                number = 1,
                icon = Icons.Filled.Assignment,
                title = "Mulai Kuesioner",
                description = "Klik tombol 'Mulai Diagnosis' pada halaman utama untuk memulai kuesioner diagnosis penyakit.",
                color = VenamePrimary
            ),
            StepData(
                number = 2,
                icon = Icons.Filled.Person,
                title = "Isi Data Petambak",
                description = "Masukkan informasi dasar seperti nama petambak, nomor HP, lokasi tambak, dan usia udang (dalam hari).",
                color = VenameSecondary
            ),
            StepData(
                number = 3,
                icon = Icons.Filled.ChecklistRtl,
                title = "Pilih Gejala",
                description = "Pilih gejala-gejala yang dialami udang vaname Anda. Untuk setiap gejala, tentukan tingkat keyakinan (Tidak Tahu, Mungkin, Kemungkinan Besar, Hampir Pasti, Pasti).",
                color = VenameAccent
            ),
            StepData(
                number = 4,
                icon = Icons.Filled.Calculate,
                title = "Sistem Menghitung",
                description = "Sistem akan menghitung diagnosis menggunakan metode Certainty Factor berdasarkan gejala yang Anda pilih dan tingkat keyakinan.",
                color = Color(0xFF7E57C2)
            ),
            StepData(
                number = 5,
                icon = Icons.Filled.Analytics,
                title = "Lihat Hasil",
                description = "Hasil diagnosis akan menampilkan penyakit yang paling mungkin dengan persentase keyakinan, beserta rekomendasi penanganan.",
                color = StatusSuccess
            ),
            StepData(
                number = 6,
                icon = Icons.Filled.PictureAsPdf,
                title = "Ekspor PDF (Opsional)",
                description = "Anda dapat mengekspor hasil diagnosis ke format PDF untuk dokumentasi atau konsultasi lebih lanjut.",
                color = StatusError
            )
        )

        steps.forEach { step ->
            StepCard(step = step)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // === TIPS ===
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
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tips Penggunaan",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                val tips = listOf(
                    "Amati gejala udang dengan teliti sebelum memulai diagnosis",
                    "Pilih tingkat keyakinan yang sesuai dengan kondisi sebenarnya",
                    "Semakin banyak gejala yang dipilih, semakin akurat hasilnya",
                    "Jika ragu, pilih tingkat keyakinan yang lebih rendah",
                    "Simpan hasil diagnosis dalam bentuk PDF untuk referensi",
                    "Konsultasikan hasil dengan ahli perikanan jika diperlukan"
                )

                tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(16.dp).offset(y = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tip,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // === NOTES ===
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            color = StatusWarning.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = StatusWarning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Catatan Penting",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hasil diagnosis dari sistem ini bersifat informatif dan tidak menggantikan konsultasi dengan ahli perikanan atau dokter hewan. Untuk penanganan yang tepat, selalu konsultasikan dengan tenaga ahli.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
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

data class StepData(
    val number: Int,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun StepCard(step: StepData) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Step number badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(step.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        step.icon,
                        contentDescription = null,
                        tint = step.color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${step.number}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = step.color
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}