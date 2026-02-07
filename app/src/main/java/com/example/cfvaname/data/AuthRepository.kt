package com.example.cfvaname.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repository untuk autentikasi via Supabase RPC
 * Password di-hash di sisi server (Supabase/PostgreSQL) menggunakan pgcrypto bcrypt
 */
class AuthRepository {

    private val client = SupabaseClient.supabase

    /**
     * Login menggunakan email dan password.
     * Memanggil RPC function `login_user` di Supabase yang memverifikasi
     * password menggunakan bcrypt hash comparison.
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val params = buildJsonObject {
                put("p_email", email.trim().lowercase())
                put("p_password", password)
            }

            val response = client.postgrest.rpc(
                function = "login_user",
                parameters = params
            ).decodeAs<LoginResponse>()

            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.localizedMessage}"))
        }
    }
}