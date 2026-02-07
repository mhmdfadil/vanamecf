package com.example.cfvaname.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Menyimpan session user di SharedPreferences
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vename_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ROLE = "role"
    }

    fun saveSession(user: UserSession) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, user.userId)
            putString(KEY_EMAIL, user.email)
            putString(KEY_FULL_NAME, user.fullName)
            putString(KEY_ROLE, user.role)
            apply()
        }
    }

    fun getSession(): UserSession? {
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) return null
        return UserSession(
            userId = prefs.getString(KEY_USER_ID, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            fullName = prefs.getString(KEY_FULL_NAME, "") ?: "",
            role = prefs.getString(KEY_ROLE, "") ?: ""
        )
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}