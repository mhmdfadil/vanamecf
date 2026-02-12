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
 * PERUBAHAN: Tidak ada lagi hipotesis_id di tabel gejalas.
 * Relasi gejala <-> hipotesis sekarang many-to-many via tabel gejala_hipotesis.
 */
@Serializable
data class Gejala(
    val id: Long = 0,
    val kode: String = "",
    val nama: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Request body untuk insert/update gejala
 * PERUBAHAN: Tidak ada lagi hipotesis_id
 */
@Serializable
data class GejalaRequest(
    val kode: String,
    val nama: String
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

// ===================================================
// GEJALA_HIPOTESIS MODELS (PIVOT TABLE - many-to-many)
// ===================================================

/**
 * Model GejalaHipotesis - mapping ke tabel gejala_hipotesis di Supabase
 * Tabel pivot yang menghubungkan gejala dengan hipotesis (many-to-many)
 */
@Serializable
data class GejalaHipotesis(
    val id: Long = 0,
    @SerialName("gejala_id") val gejalaId: Long = 0,
    @SerialName("hipotesis_id") val hipotesisId: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Request body untuk insert gejala_hipotesis
 */
@Serializable
data class GejalaHipotesisRequest(
    @SerialName("gejala_id") val gejalaId: Long,
    @SerialName("hipotesis_id") val hipotesisId: Long
)

// ===================================================
// NILAI CF MODELS
// ===================================================

/**
 * Model Nilai CF - mapping ke tabel nilai_cfs di Supabase
 * Nilai dibagi rata dari 0.00 sampai 1.00
 */
@Serializable
data class NilaiCf(
    val id: Long = 0,
    val keterangan: String = "",
    val nilai: Double = 0.0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Request body untuk insert/update nilai CF (hanya keterangan, nilai dihitung otomatis)
 */
@Serializable
data class NilaiCfInsertRequest(
    val keterangan: String,
    val nilai: Double = 0.0
)

// ===================================================
// RULE MODELS
// ===================================================

/**
 * Model Rule - mapping ke tabel rules di Supabase
 * PERUBAHAN: Sekarang merujuk ke gejala_hipotesis_id (bukan gejala_id)
 * Artinya: rule = "gejala X untuk hipotesis Y" punya CF pakar tertentu
 */
@Serializable
data class Rule(
    val id: Long = 0,
    @SerialName("gejala_hipotesis_id") val gejalaHipotesisId: Long = 0,
    @SerialName("cf_id") val cfId: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Request body untuk insert/update rule
 * PERUBAHAN: Sekarang merujuk ke gejala_hipotesis_id
 */
@Serializable
data class RuleRequest(
    @SerialName("gejala_hipotesis_id") val gejalaHipotesisId: Long,
    @SerialName("cf_id") val cfId: Long
)

// ===================================================
// KUESIONER MODELS
// ===================================================

@Serializable
data class Kuesioner(
    val id: Long = 0,
    @SerialName("nama_petambak") val namaPetambak: String = "",
    @SerialName("no_hp") val noHp: String = "",
    @SerialName("lokasi_tambak") val lokasiTambak: String = "",
    @SerialName("usia_udang") val usiaUdang: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class KuesionerInsertRequest(
    @SerialName("nama_petambak") val namaPetambak: String,
    @SerialName("no_hp") val noHp: String,
    @SerialName("lokasi_tambak") val lokasiTambak: String,
    @SerialName("usia_udang") val usiaUdang: Int
)

/**
 * Model KuesionerData - mapping ke tabel kuesioner_data di Supabase
 * PERUBAHAN: Sekarang merujuk ke gejala_hipotesis_id (bukan gejala_id)
 */
@Serializable
data class KuesionerData(
    val id: Long = 0,
    @SerialName("kuesioner_id") val kuesionerId: Long = 0,
    @SerialName("gejala_hipotesis_id") val gejalaHipotesisId: Long = 0,
    @SerialName("cf_value") val cfValue: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Request body untuk insert kuesioner_data
 * PERUBAHAN: Sekarang merujuk ke gejala_hipotesis_id
 */
@Serializable
data class KuesionerDataInsertRequest(
    @SerialName("kuesioner_id") val kuesionerId: Long,
    @SerialName("gejala_hipotesis_id") val gejalaHipotesisId: Long,
    @SerialName("cf_value") val cfValue: Long
)

// ===================================================
// CF CALCULATION HELPER MODELS (non-Supabase, for UI)
// ===================================================

data class CfCalculationStep(
    val gejalaKode: String,
    val gejalaNama: String,
    val cfPakar: Double,
    val cfUser: Double,
    val cfGejala: Double,
    val cfSebelum: Double,
    val cfSesudah: Double
)

data class HipotesisResult(
    val hipotesis: Hipotesis,
    val cfCombine: Double,
    val percentage: Double,
    val steps: List<CfCalculationStep>
)