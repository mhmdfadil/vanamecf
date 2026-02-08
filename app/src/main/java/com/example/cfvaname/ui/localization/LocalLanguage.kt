package com.example.cfvaname.ui.localization

import androidx.compose.runtime.*

// CompositionLocal untuk bahasa
val LocalLanguage = compositionLocalOf { "id" }

// Helper composable function untuk mendapatkan string berdasarkan bahasa aktif
@Composable
fun stringResource(strings: AppStrings): String {
    val language = LocalLanguage.current
    return AppStrings.get(strings, language)
}