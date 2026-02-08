package com.example.cfvaname.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cfvaname.data.UserSession
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.ui.localization.*

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
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
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
            onClick = { /* TODO: Handle notifications */ }
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
            subtitle = stringResource(AppStrings.ManageLocalData),
            onClick = { /* TODO: Handle storage */ }
        )
        
        // Privacy Setting
        SettingsItem(
            icon = Icons.Filled.Shield,
            title = stringResource(AppStrings.Privacy),
            subtitle = stringResource(AppStrings.PrivacySettings),
            onClick = { /* TODO: Handle privacy */ }
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