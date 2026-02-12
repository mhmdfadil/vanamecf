package com.example.cfvaname.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

/**
 * Repository CRUD untuk tabel gejala_hipotesis (pivot many-to-many) di Supabase
 * Tabel ini menghubungkan gejala dengan hipotesis.
 */
class GejalaHipotesisRepository {

    private val client = SupabaseClient.supabase
    private val table = "gejala_hipotesis"

    /**
     * Ambil semua relasi gejala-hipotesis
     */
    suspend fun getAll(): Result<List<GejalaHipotesis>> {
        return try {
            val result = client.postgrest.from(table)
                .select {
                    order("id", Order.ASCENDING)
                }
                .decodeList<GejalaHipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data gejala-hipotesis: ${e.localizedMessage}"))
        }
    }

    /**
     * Ambil relasi berdasarkan hipotesis_id
     */
    suspend fun getByHipotesisId(hipotesisId: Long): Result<List<GejalaHipotesis>> {
        return try {
            val result = client.postgrest.from(table)
                .select {
                    filter { eq("hipotesis_id", hipotesisId) }
                    order("id", Order.ASCENDING)
                }
                .decodeList<GejalaHipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat gejala untuk hipotesis: ${e.localizedMessage}"))
        }
    }

    /**
     * Ambil relasi berdasarkan gejala_id
     */
    suspend fun getByGejalaId(gejalaId: Long): Result<List<GejalaHipotesis>> {
        return try {
            val result = client.postgrest.from(table)
                .select {
                    filter { eq("gejala_id", gejalaId) }
                    order("id", Order.ASCENDING)
                }
                .decodeList<GejalaHipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat hipotesis untuk gejala: ${e.localizedMessage}"))
        }
    }

    /**
     * Tambah relasi gejala-hipotesis baru
     */
    suspend fun insert(request: GejalaHipotesisRequest): Result<GejalaHipotesis> {
        return try {
            val result = client.postgrest.from(table)
                .insert(request) {
                    select()
                }
                .decodeSingle<GejalaHipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            val msg = if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                "Relasi gejala-hipotesis ini sudah ada"
            } else {
                "Gagal menambah relasi: ${e.localizedMessage}"
            }
            Result.failure(Exception(msg))
        }
    }

    /**
     * Hapus relasi berdasarkan id
     */
    suspend fun delete(id: Long): Result<Boolean> {
        return try {
            client.postgrest.from(table)
                .delete {
                    filter { eq("id", id) }
                }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus relasi: ${e.localizedMessage}"))
        }
    }

    /**
     * Hapus semua relasi untuk gejala tertentu
     */
    suspend fun deleteByGejalaId(gejalaId: Long): Result<Boolean> {
        return try {
            client.postgrest.from(table)
                .delete {
                    filter { eq("gejala_id", gejalaId) }
                }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus relasi gejala: ${e.localizedMessage}"))
        }
    }

    /**
     * Hapus semua relasi untuk hipotesis tertentu
     */
    suspend fun deleteByHipotesisId(hipotesisId: Long): Result<Boolean> {
        return try {
            client.postgrest.from(table)
                .delete {
                    filter { eq("hipotesis_id", hipotesisId) }
                }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus relasi hipotesis: ${e.localizedMessage}"))
        }
    }
}