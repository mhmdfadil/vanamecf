package com.example.cfvaname.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response dari RPC function login_user di Supabase
 */
@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    @SerialName("user_id") val userId: String? = null,
    val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val role: String? = null
)

/**
 * Request body untuk RPC login_user
 */
@Serializable
data class LoginRequest(
    @SerialName("p_email") val email: String,
    @SerialName("p_password") val password: String
)

/**
 * Data user yang disimpan di session setelah login
 */
data class UserSession(
    val userId: String,
    val email: String,
    val fullName: String,
    val role: String
)

// ===================================================
// GEJALA MODELS
// ===================================================

/**
 * Model Gejala - mapping ke tabel gejalas di Supabase
 */
@Serializable
data class Gejala(
    val id: Long = 0,
    val kode: String = "",
    val nama: String = "",
    @SerialName("hipotesis_id") val hipotesisId: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Request body untuk insert/update gejala
 */
@Serializable
data class GejalaRequest(
    val kode: String,
    val nama: String,
    @SerialName("hipotesis_id") val hipotesisId: Long
)

// ===================================================
// HIPOTESIS MODELS
// ===================================================

/**
 * Model Hipotesis - mapping ke tabel hipotesiss di Supabase
 */
@Serializable
data class Hipotesis(
    val id: Long = 0,
    val kode: String = "",
    val nama: String = "",
    val deskripsi: String? = null,
    val rekomendasi: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Request body untuk insert/update hipotesis
 */
@Serializable
data class HipotesisRequest(
    val kode: String,
    val nama: String,
    val deskripsi: String? = null,
    val rekomendasi: String? = null
)