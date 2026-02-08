package com.example.cfvaname.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"
    }
    
    // Language: "id" atau "en"
    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "id") ?: "id"
    }
    
    fun setLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }
    
    // Theme: "light", "dark", atau "system"
    fun getTheme(): String {
        return prefs.getString(KEY_THEME, "system") ?: "system"
    }
    
    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }
}