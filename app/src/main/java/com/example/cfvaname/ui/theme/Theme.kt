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
    secondary = VenameSecondary,
    onSecondary = TextOnPrimary,
    background = VenameBgLight,
    surface = VenameSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = StatusError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = VenamePrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = VenamePrimaryDark,
    secondary = VenameSecondary,
    onSecondary = TextOnPrimary,
    background = VenameBgDark,
    surface = VenameSurfaceDark,
    onBackground = TextOnDark,
    onSurface = TextOnDark,
    error = StatusError,
    onError = Color.White
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