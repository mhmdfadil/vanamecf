package com.example.cfvaname.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.cfvaname.data.*
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.viewmodel.KuesionerUserViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ======================================================================
// HASIL SCREEN - RINGKASAN HASIL (SIMPLIFIED)
// ======================================================================
@Composable
fun KuesionerUserHasilScreen(
    viewModel: KuesionerUserViewModel,
    onBack: () -> Unit,
    onNewDiagnosis: () -> Unit,
    onViewReports: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val k = uiState.hasilKuesioner ?: return

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(StatusSuccess, Color(0xFF43A047))
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            "Kembali",
                            tint = Color.White
                        )
                    }

                    TextButton(
                        onClick = { exportPdfUser(context, k, uiState.hasilResults, uiState.allGejalaMap, uiState.allNilaiCfMap) }
                    ) {
                        Icon(
                            Icons.Filled.PictureAsPdf,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Export PDF", color = Color.White, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            "Diagnosis Selesai",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Hasil Perhitungan Certainty Factor",
                            color = Color.White.copy(0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VenamePrimary)
            }
            return@Column
        }

        // Info Petambak
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            color = VenamePrimary.copy(0.06f)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Informasi Petambak",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = VenamePrimary
                )
                Spacer(Modifier.height(8.dp))
                InfoRowUser("Nama", k.namaPetambak)
                InfoRowUser("No. HP", k.noHp)
                InfoRowUser("Lokasi", k.lokasiTambak)
                InfoRowUser("Usia Udang", "${k.usiaUdang} hari")
                InfoRowUser("Tanggal", SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date()))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Hasil Diagnosis (Ranking)
        if (uiState.hasilResults.isNotEmpty()) {
            Text(
                "Hasil Diagnosis",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            uiState.hasilResults.forEachIndexed { idx, res ->
                val color = when {
                    res.percentage > 50 -> StatusSuccess
                    res.percentage > 30 -> StatusWarning
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(0.06f)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "#${idx + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = color
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${res.hipotesis.kode} - ${res.hipotesis.nama}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (res.percentage / 100f).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = color.copy(0.15f)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${res.percentage}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = color
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Kesimpulan
            val mainResult = uiState.hasilResults.first()
            Surface(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = StatusSuccess.copy(0.06f),
                shadowElevation = 2.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Kesimpulan Diagnosis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = StatusSuccess
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Penyakit Terdeteksi: ${mainResult.hipotesis.nama}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Tingkat Keyakinan: ${mainResult.percentage}%",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!mainResult.hipotesis.deskripsi.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Deskripsi:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            mainResult.hipotesis.deskripsi,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }

                    if (!mainResult.hipotesis.rekomendasi.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = VenamePrimary.copy(0.06f)
                        ) {
                            Row(Modifier.padding(10.dp)) {
                                Icon(
                                    Icons.Filled.Lightbulb,
                                    null,
                                    tint = VenamePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Rekomendasi:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VenamePrimary
                                    )
                                    Text(
                                        mainResult.hipotesis.rekomendasi,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Disclaimer
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            color = StatusWarning.copy(0.08f)
        ) {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Filled.Warning,
                    null,
                    tint = StatusWarning,
                    modifier = Modifier.size(18.dp).offset(y = 2.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Catatan Penting",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Hasil diagnosis bersifat informatif. Untuk penanganan yang tepat, konsultasikan dengan ahli perikanan atau dokter hewan.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action Buttons
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Button(
                onClick = onNewDiagnosis,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Diagnosis Baru", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onViewReports,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VenamePrimary)
            ) {
                Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Lihat Riwayat Laporan", fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun InfoRowUser(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ======================================================================
// PDF EXPORT (SIMPLIFIED VERSION)
// ======================================================================
fun exportPdfUser(
    context: Context,
    kuesioner: Kuesioner,
    results: List<HipotesisResult>,
    gejalaMap: Map<Long, Gejala>,
    nilaiCfMap: Map<Long, NilaiCf>
) {
    try {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val marginLeft = 45f
        val marginRight = 45f
        val marginTop = 50f
        val contentWidth = pageWidth - marginLeft - marginRight

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = marginTop

        val paint = Paint().apply { isAntiAlias = true }
        val fillPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }

        // Header
        fillPaint.color = android.graphics.Color.parseColor("#1A73E8")
        canvas.drawRect(marginLeft, y, pageWidth - marginRight, y + 70f, fillPaint)
        
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.WHITE
        canvas.drawText("Laporan Hasil Diagnosis", marginLeft + 18f, y + 28f, paint)
        
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Sistem Pakar Penyakit Udang Vaname", marginLeft + 18f, y + 45f, paint)
        
        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        val dateWidth = paint.measureText(dateStr)
        canvas.drawText(dateStr, pageWidth - marginRight - dateWidth - 18f, y + 45f, paint)
        
        y += 90f

        // Info Petambak
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#0D47A1")
        canvas.drawText("INFORMASI PETAMBAK", marginLeft, y, paint)
        y += 20f

        val infoItems = listOf(
            "Nama Petambak" to kuesioner.namaPetambak,
            "No. HP" to kuesioner.noHp,
            "Lokasi Tambak" to kuesioner.lokasiTambak,
            "Usia Udang" to "${kuesioner.usiaUdang} hari"
        )

        paint.textSize = 10f
        infoItems.forEach { (label, value) ->
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.GRAY
            canvas.drawText(label, marginLeft + 14f, y, paint)
            
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.BLACK
            canvas.drawText(value, marginLeft + 140f, y, paint)
            y += 18f
        }

        y += 10f

        // Hasil Diagnosis
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#0D47A1")
        canvas.drawText("HASIL DIAGNOSIS", marginLeft, y, paint)
        y += 20f

        results.forEachIndexed { idx, result ->
            val rankColor = when {
                result.percentage > 50 -> android.graphics.Color.parseColor("#2E7D32")
                result.percentage > 30 -> android.graphics.Color.parseColor("#E65100")
                else -> android.graphics.Color.GRAY
            }

            paint.textSize = 11f
            paint.isFakeBoldText = true
            paint.color = rankColor
            canvas.drawText("#${idx + 1}", marginLeft + 10f, y, paint)

            paint.color = android.graphics.Color.BLACK
            val title = "${result.hipotesis.kode} - ${result.hipotesis.nama}"
            canvas.drawText(title, marginLeft + 40f, y, paint)

            paint.color = rankColor
            val pct = "${result.percentage}%"
            val pctWidth = paint.measureText(pct)
            canvas.drawText(pct, pageWidth - marginRight - pctWidth - 10f, y, paint)

            y += 20f
        }

        y += 10f

        // Kesimpulan
        if (results.isNotEmpty()) {
            val mainResult = results.first()
            
            paint.textSize = 12f
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.parseColor("#2E7D32")
            canvas.drawText("KESIMPULAN", marginLeft, y, paint)
            y += 20f

            paint.textSize = 11f
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.BLACK
            canvas.drawText("Penyakit Terdeteksi: ${mainResult.hipotesis.nama}", marginLeft + 14f, y, paint)
            y += 18f

            paint.isFakeBoldText = false
            canvas.drawText("Tingkat Keyakinan: ${mainResult.percentage}%", marginLeft + 14f, y, paint)
            y += 20f

            if (!mainResult.hipotesis.rekomendasi.isNullOrBlank()) {
                paint.isFakeBoldText = true
                canvas.drawText("Rekomendasi:", marginLeft + 14f, y, paint)
                y += 16f

                paint.isFakeBoldText = false
                paint.textSize = 10f
                val words = mainResult.hipotesis.rekomendasi.split(" ")
                var currentLine = ""
                words.forEach { word ->
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (paint.measureText(testLine) <= contentWidth - 28f) {
                        currentLine = testLine
                    } else {
                        canvas.drawText(currentLine, marginLeft + 14f, y, paint)
                        y += 14f
                        currentLine = word
                    }
                }
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, marginLeft + 14f, y, paint)
                    y += 14f
                }
            }
        }

        y += 20f

        // Footer
        paint.textSize = 8f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Dokumen ini digenerate oleh Sistem Cerdas Vaname", marginLeft, y, paint)
        y += 12f
        canvas.drawText("Hasil diagnosis bersifat informatif, konsultasikan dengan ahli untuk penanganan yang tepat.", marginLeft, y, paint)

        document.finishPage(page)

        // Save PDF
        val documentsDir = File(context.getExternalFilesDir(null), "VENAME_Reports")
        documentsDir.mkdirs()
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val fileName = "Diagnosa_${kuesioner.namaPetambak.replace(" ", "_")}_$timestamp.pdf"
        val file = File(documentsDir, fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        Toast.makeText(context, "PDF berhasil disimpan!", Toast.LENGTH_SHORT).show()

        // Open PDF
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "PDF tersimpan di: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal export PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}