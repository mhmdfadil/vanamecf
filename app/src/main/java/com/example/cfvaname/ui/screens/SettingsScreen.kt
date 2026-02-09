package com.example.cfvaname.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cfvaname.R
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.ui.localization.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File

// ===================================================
// SETTINGS SCREEN
// ===================================================
@Composable
fun SettingsScreen(
    padding: PaddingValues,
    onLogout: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    
    // Storage state
    var storageInfo by remember { mutableStateOf<StorageInfoData?>(null) }
    
    LaunchedEffect(Unit) {
        storageInfo = calculateStorageInfo(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(AppStrings.SettingsTitle),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Notifications Setting
        SettingsItem(
            icon = Icons.Filled.Notifications,
            title = stringResource(AppStrings.Notifications),
            subtitle = stringResource(AppStrings.ManageNotifications),
            onClick = { 
                // Buat notifikasi channel jika belum ada
                createNotificationChannel(context)
                // Kirim welcome notification
                sendWelcomeNotification(context)
                // Buka system notification settings
                openNotificationSettings(context)
            }
        )
        
        // Language Setting
        SettingsItem(
            icon = Icons.Filled.Language,
            title = stringResource(AppStrings.Language),
            subtitle = if (currentLanguage == "en") 
                stringResource(AppStrings.LanguageEnglish)
            else 
                stringResource(AppStrings.LanguageIndonesian),
            onClick = { showLanguageDialog = true }
        )
        
        // Theme Setting
        SettingsItem(
            icon = Icons.Filled.DarkMode,
            title = stringResource(AppStrings.Theme),
            subtitle = when (currentTheme) {
                "light" -> stringResource(AppStrings.ThemeLight)
                "dark" -> stringResource(AppStrings.ThemeDark)
                else -> stringResource(AppStrings.ThemeSystem)
            },
            onClick = { showThemeDialog = true }
        )
        
        // Storage Setting
        SettingsItem(
            icon = Icons.Filled.Storage,
            title = stringResource(AppStrings.Storage),
            subtitle = storageInfo?.let { 
                "${it.usedFormatted} digunakan • ${it.availableFormatted} tersisa"
            } ?: stringResource(AppStrings.ManageLocalData),
            onClick = { showStorageDialog = true }
        )
        
        // Privacy Setting
        SettingsItem(
            icon = Icons.Filled.Shield,
            title = stringResource(AppStrings.Privacy),
            subtitle = stringResource(AppStrings.PrivacySettings),
            onClick = { showPrivacyDialog = true }
        )
        
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
            Text(stringResource(AppStrings.Logout), fontWeight = FontWeight.Bold)
        }
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                onLanguageChange(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeDialog(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
                onThemeChange(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // Storage Info Dialog
    if (showStorageDialog) {
        StorageDialog(
            storageInfo = storageInfo,
            onDismiss = { showStorageDialog = false },
            onRefresh = {
                // Refresh storage info
                coroutineScope.launch {
                    storageInfo = calculateStorageInfo(context)
                }
            }
        )
    }
    
    // Privacy Settings Dialog
    if (showPrivacyDialog) {
        PrivacyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = VenamePrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontWeight = FontWeight.Medium, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle, 
                    fontSize = 13.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ===================================================
// LANGUAGE DIALOG
// ===================================================
@Composable
fun LanguageDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(AppStrings.Language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                LanguageOption(
                    text = stringResource(AppStrings.LanguageIndonesian),
                    isSelected = currentLanguage == "id",
                    onClick = { onLanguageSelected("id") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LanguageOption(
                    text = stringResource(AppStrings.LanguageEnglish),
                    isSelected = currentLanguage == "en",
                    onClick = { onLanguageSelected("en") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(AppStrings.Cancel))
            }
        }
    )
}

@Composable
fun LanguageOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val selectedBgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val selectedTextColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = selectedBgColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) VenamePrimary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = VenamePrimary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = selectedTextColor
            )
        }
    }
}

// ===================================================
// THEME DIALOG
// ===================================================
@Composable
fun ThemeDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(AppStrings.Theme),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ThemeOption(
                    text = stringResource(AppStrings.ThemeLight),
                    icon = Icons.Filled.LightMode,
                    isSelected = currentTheme == "light",
                    onClick = { onThemeSelected("light") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption(
                    text = stringResource(AppStrings.ThemeDark),
                    icon = Icons.Filled.DarkMode,
                    isSelected = currentTheme == "dark",
                    onClick = { onThemeSelected("dark") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption(
                    text = stringResource(AppStrings.ThemeSystem),
                    icon = Icons.Filled.Brightness4,
                    isSelected = currentTheme == "system",
                    onClick = { onThemeSelected("system") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(AppStrings.Cancel))
            }
        }
    )
}

@Composable
fun ThemeOption(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val selectedBgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val selectedTextColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val iconColor = if (isSelected) {
        VenamePrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = selectedBgColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) VenamePrimary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = selectedTextColor,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = VenamePrimary
                )
            )
        }
    }
}

// ===================================================
// STORAGE DIALOG
// ===================================================
data class StorageInfoData(
    val totalFiles: Int,
    val usedBytes: Long,
    val usedFormatted: String,
    val availableBytes: Long,
    val availableFormatted: String,
    val totalBytes: Long,
    val totalFormatted: String,
    val usagePercentage: Float
)

suspend fun calculateStorageInfo(context: Context): StorageInfoData = withContext(Dispatchers.IO) {
    // Hitung storage dari VENAME_Reports
    val reportsDir = File(context.getExternalFilesDir(null), "VENAME_Reports")
    val pdfFiles = reportsDir.listFiles { f -> f.extension.equals("pdf", true) } ?: emptyArray()
    val usedBytes = pdfFiles.sumOf { it.length() }
    
    // Hitung available storage (internal storage)
    val internalDir = context.filesDir
    val availableBytes = internalDir.usableSpace
    val totalBytes = internalDir.totalSpace
    
    StorageInfoData(
        totalFiles = pdfFiles.size,
        usedBytes = usedBytes,
        usedFormatted = formatFileSize(usedBytes),
        availableBytes = availableBytes,
        availableFormatted = formatFileSize(availableBytes),
        totalBytes = totalBytes,
        totalFormatted = formatFileSize(totalBytes),
        usagePercentage = if (totalBytes > 0) (totalBytes - availableBytes).toFloat() / totalBytes else 0f
    )
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
}

@Composable
fun StorageDialog(
    storageInfo: StorageInfoData?,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val language = LocalLanguage.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(AppStrings.Storage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = VenamePrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (storageInfo != null) {
                    // Reports Storage Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = VenamePrimary.copy(alpha = 0.08f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.PictureAsPdf,
                                    contentDescription = null,
                                    tint = StatusError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == "en") "PDF Reports" else "Laporan PDF",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = storageInfo.usedFormatted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = VenamePrimary
                                    )
                                    Text(
                                        text = if (language == "en") "Used" else "Digunakan",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = VenamePrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${storageInfo.totalFiles} ${if (language == "en") "files" else "file"}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VenamePrimary
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Device Storage Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.SdCard,
                                    contentDescription = null,
                                    tint = VenameSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == "en") "Device Storage" else "Penyimpanan Perangkat",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Storage Bar
                            Column {
                                LinearProgressIndicator(
                                    progress = { storageInfo.usagePercentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = when {
                                        storageInfo.usagePercentage > 0.9f -> StatusError
                                        storageInfo.usagePercentage > 0.7f -> StatusWarning
                                        else -> StatusSuccess
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = storageInfo.availableFormatted,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = StatusSuccess
                                        )
                                        Text(
                                            text = if (language == "en") "Available" else "Tersedia",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = storageInfo.totalFormatted,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (language == "en") "Total" else "Total",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VenamePrimary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(AppStrings.Close))
            }
        }
    )
}

// ===================================================
// PRIVACY DIALOG
// ===================================================
@Composable
fun PrivacyDialog(
    onDismiss: () -> Unit
) {
    val language = LocalLanguage.current
    
    // Dummy privacy settings states
    var dataCollection by remember { mutableStateOf(true) }
    var analytics by remember { mutableStateOf(true) }
    var crashReports by remember { mutableStateOf(true) }
    var thirdPartySharing by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    tint = VenamePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(AppStrings.Privacy),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (language == "en") 
                        "Manage how your data is collected and used"
                    else 
                        "Kelola bagaimana data Anda dikumpulkan dan digunakan",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Data Collection
                PrivacySettingItem(
                    title = if (language == "en") "Data Collection" else "Pengumpulan Data",
                    description = if (language == "en") 
                        "Allow app to collect usage data for improvement"
                    else 
                        "Izinkan aplikasi mengumpulkan data penggunaan untuk perbaikan",
                    checked = dataCollection,
                    onCheckedChange = { dataCollection = it }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Analytics
                PrivacySettingItem(
                    title = if (language == "en") "Analytics" else "Analitik",
                    description = if (language == "en") 
                        "Help us understand app performance"
                    else 
                        "Bantu kami memahami performa aplikasi",
                    checked = analytics,
                    onCheckedChange = { analytics = it }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Crash Reports
                PrivacySettingItem(
                    title = if (language == "en") "Crash Reports" else "Laporan Crash",
                    description = if (language == "en") 
                        "Automatically send crash reports to developers"
                    else 
                        "Kirim laporan crash otomatis ke pengembang",
                    checked = crashReports,
                    onCheckedChange = { crashReports = it }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Third Party Sharing
                PrivacySettingItem(
                    title = if (language == "en") "Third-Party Sharing" else "Berbagi Pihak Ketiga",
                    description = if (language == "en") 
                        "Share anonymized data with partners"
                    else 
                        "Bagikan data anonim dengan mitra",
                    checked = thirdPartySharing,
                    onCheckedChange = { thirdPartySharing = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = VenamePrimary.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = VenamePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "en")
                                "Your privacy is important to us. All data is encrypted and stored securely."
                            else
                                "Privasi Anda penting bagi kami. Semua data dienkripsi dan disimpan dengan aman.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VenamePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (language == "en") "Save" else "Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(AppStrings.Cancel))
            }
        }
    )
}

@Composable
fun PrivacySettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = VenamePrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// ===================================================
// NOTIFICATION HELPER FUNCTIONS
// ===================================================
private const val CHANNEL_ID = "vename_notifications"
private const val NOTIFICATION_ID = 1

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Vename Notifications"
        val descriptionText = "Notifikasi dari Sistem Cerdas Vename"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun sendWelcomeNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti dengan icon Anda
        .setContentTitle("Selamat Datang! 🦐")
        .setContentText("Selamat datang di Sistem Pakar Udang Vaname")
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText("Selamat datang di Sistem Pakar Udang Vaname! Aplikasi ini membantu Anda mendiagnosa penyakit udang vaname dengan metode Certainty Factor.")
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
    
    with(NotificationManagerCompat.from(context)) {
        try {
            notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}

fun openNotificationSettings(context: Context) {
    val intent = Intent().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    context.startActivity(intent)
}