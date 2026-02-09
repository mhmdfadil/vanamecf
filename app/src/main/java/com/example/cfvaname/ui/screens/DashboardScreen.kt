package com.example.cfvaname.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cfvaname.data.UserSession
import com.example.cfvaname.ui.localization.AppStrings
import com.example.cfvaname.ui.localization.stringResource
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.viewmodel.DashboardViewModel
import com.example.cfvaname.viewmodel.HipotesisWithGejalaCount
import com.example.cfvaname.viewmodel.KuesionerWithDetail
import java.text.SimpleDateFormat
import java.util.*

// =====================================================
// CHART COLORS
// =====================================================
private val chartColors = listOf(
    Color(0xFF667EEA), // primary purple
    Color(0xFFF6D365), // warning yellow
    Color(0xFFF093FB), // pink
    Color(0xFF4FACFE), // blue
    Color(0xFFA1C4FD), // light blue
    Color(0xFF43E97B), // green
    Color(0xFFFA709A), // red-pink
    Color(0xFFFEE140), // bright yellow
)

@Composable
fun DashboardScreen(
    userSession: UserSession?,
    padding: PaddingValues,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val language = com.example.cfvaname.ui.localization.LocalLanguage.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        // =====================================================
        // HEADER - Welcome Banner + Date
        // =====================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(AppStrings.WelcomeBack),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = VenamePrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${if (language == "en") "Welcome" else "Selamat datang"}, ${userSession?.fullName ?: "User"}!",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            // Date chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = getCurrentDateFormatted(language),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = VenamePrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =====================================================
        // STATS CARDS - 2x2 grid
        // =====================================================
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VenamePrimary)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.BugReport,
                    value = "${uiState.stats.totalGejala}",
                    label = stringResource(AppStrings.TotalSymptoms),
                    color = VenamePrimary,
                    badgeText = "Total",
                    badgeSubText = if (language == "en") "registered symptoms" else "gejala terdaftar"
                )
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Science,
                    value = "${uiState.stats.totalHipotesis}",
                    label = stringResource(AppStrings.TotalHypothesis),
                    color = Color(0xFFF6D365),
                    badgeText = "Total",
                    badgeSubText = if (language == "en") "registered hypothesis" else "hipotesis terdaftar"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Assignment,
                    value = "${uiState.stats.totalKuesioner}",
                    label = if (language == "en") "Total Questionnaire" else "Total Kuesioner",
                    color = Color(0xFFF093FB),
                    badgeText = "Total",
                    badgeSubText = if (language == "en") "questionnaires filled" else "kuesioner terisi"
                )
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Functions,
                    value = "${uiState.stats.totalNilaiCf}",
                    label = stringResource(AppStrings.NilaiCf),
                    color = Color(0xFF4FACFE),
                    badgeText = "Total",
                    badgeSubText = if (language == "en") "CF values" else "nilai CF"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // =====================================================
        // BAR CHART - Distribusi Gejala per Hipotesis
        // =====================================================
        if (!uiState.isLoading && uiState.topHipotesis.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "en") "Symptoms Distribution per Hypothesis"
                            else "Distribusi Gejala per Hipotesis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bar Chart
                    GejalaBarChart(
                        data = uiState.topHipotesis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.topHipotesis.forEachIndexed { index, item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(chartColors[index % chartColors.size])
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${item.hipotesis.kode} - ${item.hipotesis.nama}",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${item.gejalaCount}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // =====================================================
        // TWO COLUMNS: Recent Kuesioner & Top Hipotesis
        // =====================================================

        // Recent Kuesioner
        if (!uiState.isLoading) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == "en") "Recent Questionnaires" else "Kuesioner Terbaru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.recentKuesioner.isEmpty()) {
                        Text(
                            text = stringResource(AppStrings.NoQuestionnaireData),
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        uiState.recentKuesioner.forEach { item ->
                            RecentKuesionerItem(item = item, language = language)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top Hipotesis Table
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == "en") "Hypothesis with Most Symptoms"
                        else "Hipotesis dengan Gejala Terbanyak",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.topHipotesis.isEmpty()) {
                        Text(
                            text = stringResource(AppStrings.NoHypothesis),
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        // Table header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = if (language == "en") "Code" else "Kode",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = if (language == "en") "Hypothesis Name" else "Nama Hipotesis",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (language == "en") "Symptoms" else "Gejala",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        uiState.topHipotesis.forEach { item ->
                            HipotesisTableRow(item = item)
                        }
                    }
                }
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFDEDED)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = error, fontSize = 13.sp, color = Color(0xFFD32F2F))
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { dashboardViewModel.loadDashboardData() }) {
                        Text("Retry", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// =====================================================
// STAT CARD (matching Laravel style)
// =====================================================
@Composable
fun DashboardStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    badgeText: String = "",
    badgeSubText: String = ""
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
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
            }

            if (badgeText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = color.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = color
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badgeSubText,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// =====================================================
// BAR CHART COMPOSABLE
// =====================================================
@Composable
fun GejalaBarChart(
    data: List<HipotesisWithGejalaCount>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.maxOfOrNull { it.gejalaCount } ?: 1).coerceAtLeast(1)
    val density = LocalDensity.current

    // Animation
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)
        )
    }

    val textColor = TextSecondary
    val gridColor = TextSecondary.copy(alpha = 0.15f)

    Canvas(modifier = modifier) {
        val chartLeft = 40f
        val chartBottom = size.height - 40f
        val chartTop = 16f
        val chartRight = size.width - 16f
        val chartHeight = chartBottom - chartTop
        val chartWidth = chartRight - chartLeft

        val barCount = data.size
        val barGroupWidth = chartWidth / barCount
        val barWidth = (barGroupWidth * 0.55f).coerceAtMost(80f)
        val barSpacing = (barGroupWidth - barWidth) / 2

        // Grid lines (horizontal)
        val gridLineCount = 4
        for (i in 0..gridLineCount) {
            val y = chartBottom - (chartHeight * i / gridLineCount)
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1f
            )

            // Y-axis labels
            val labelValue = (maxValue * i / gridLineCount)
            drawContext.canvas.nativeCanvas.drawText(
                "$labelValue",
                chartLeft - 8f,
                y + 4f,
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = with(density) { 10.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // Bars
        data.forEachIndexed { index, item ->
            val barHeight = if (maxValue > 0)
                (item.gejalaCount.toFloat() / maxValue) * chartHeight * animationProgress.value
            else 0f

            val x = chartLeft + barSpacing + index * barGroupWidth
            val barColor = chartColors[index % chartColors.size]

            // Draw bar with rounded top
            drawRoundRect(
                color = barColor.copy(alpha = 0.8f),
                topLeft = Offset(x, chartBottom - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Bar value on top
            if (animationProgress.value > 0.8f) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${item.gejalaCount}",
                    x + barWidth / 2,
                    chartBottom - barHeight - 8f,
                    android.graphics.Paint().apply {
                        color = textColor.hashCode()
                        textSize = with(density) { 11.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }

            // X-axis label (kode)
            drawContext.canvas.nativeCanvas.drawText(
                item.hipotesis.kode,
                x + barWidth / 2,
                chartBottom + 24f,
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = with(density) { 10.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

// =====================================================
// RECENT KUESIONER ITEM (timeline style like Laravel)
// =====================================================
@Composable
fun RecentKuesionerItem(
    item: KuesionerWithDetail,
    language: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline dot
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(VenamePrimary)
            )
            if (true) { // always show line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(VenamePrimary.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.kuesioner.namaPetambak,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextPrimary
            )
            if (item.kuesioner.lokasiTambak.isNotEmpty()) {
                Text(
                    text = "${if (language == "en") "Location" else "Lokasi"}: ${item.kuesioner.lokasiTambak}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${item.gejalaCount} ${if (language == "en") "symptoms" else "gejala"}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatRelativeTime(item.kuesioner.createdAt, language),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// =====================================================
// HIPOTESIS TABLE ROW
// =====================================================
@Composable
fun HipotesisTableRow(item: HipotesisWithGejalaCount) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.hipotesis.kode,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextPrimary,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = item.hipotesis.nama,
            fontSize = 13.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = VenamePrimary.copy(alpha = 0.12f)
        ) {
            Text(
                text = "${item.gejalaCount}",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = VenamePrimary
            )
        }
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.surfaceVariant,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

// =====================================================
// UTILITY FUNCTIONS
// =====================================================
private fun getCurrentDateFormatted(language: String): String {
    val locale = if (language == "en") Locale.ENGLISH else Locale("id", "ID")
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
    return sdf.format(Date())
}

private fun formatRelativeTime(dateString: String?, language: String): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        )
        var parsedDate: Date? = null
        for (fmt in formats) {
            try {
                parsedDate = fmt.parse(dateString)
                if (parsedDate != null) break
            } catch (_: Exception) {}
        }
        if (parsedDate == null) return dateString

        val now = System.currentTimeMillis()
        val diff = now - parsedDate.time
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24

        when {
            minutes < 1 -> if (language == "en") "just now" else "baru saja"
            minutes < 60 -> "$minutes ${if (language == "en") "min ago" else "menit lalu"}"
            hours < 24 -> "$hours ${if (language == "en") "hours ago" else "jam lalu"}"
            days < 7 -> "$days ${if (language == "en") "days ago" else "hari lalu"}"
            else -> {
                val locale = if (language == "en") Locale.ENGLISH else Locale("id", "ID")
                SimpleDateFormat("d MMM yyyy", locale).format(parsedDate)
            }
        }
    } catch (_: Exception) {
        dateString
    }
}