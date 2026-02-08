package com.example.cfvaname.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = VenamePrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = VenamePrimaryLight,
    onPrimaryContainer = Color(0xFF003C7E), // Dark blue untuk contrast di light mode
    secondary = VenameSecondary,
    onSecondary = TextOnPrimary,
    background = VenameBgLight,
    surface = VenameSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = StatusError,
    onError = Color.White,
    outline = TextSecondary,
    surfaceVariant = Color(0xFFF0F4F8),
    onSurfaceVariant = TextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = VenamePrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = Color(0xFF2A4A6F), // Dark blue container untuk dark mode
    onPrimaryContainer = Color(0xFFBBDEFB), // Light blue text untuk contrast
    secondary = VenameSecondary,
    onSecondary = TextOnPrimary,
    background = VenameBgDark,
    surface = VenameSurfaceDark,
    onBackground = TextOnDark,
    onSurface = TextOnDark,
    error = StatusError,
    onError = Color.White,
    outline = TextSecondaryDark,
    surfaceVariant = Color(0xFF1E2D3D),
    onSurfaceVariant = TextSecondaryDark
)

@Composable
fun CFVanameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}