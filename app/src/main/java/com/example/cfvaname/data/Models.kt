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