package com.example.cfvaname.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

/**
 * Repository CRUD untuk tabel gejalas di Supabase
 */
class GejalaRepository {

    private val client = SupabaseClient.supabase
    private val table = "gejalas"

    /**
     * Ambil semua gejala, diurutkan berdasarkan kode
     */
    suspend fun getAll(): Result<List<Gejala>> {
        return try {
            val result = client.postgrest.from(table)
                .select {
                    order("kode", Order.ASCENDING)
                }
                .decodeList<Gejala>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data gejala: ${e.localizedMessage}"))
        }
    }

    /**
     * Cari gejala berdasarkan keyword (kode atau nama)
     */
    suspend fun search(keyword: String): Result<List<Gejala>> {
        return try {
            val result = client.postgrest.from(table)
                .select {
                    filter {
                        or {
                            Gejala::kode ilike "%$keyword%"
                            Gejala::nama ilike "%$keyword%"
                        }
                    }
                    order("kode", Order.ASCENDING)
                }
                .decodeList<Gejala>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mencari gejala: ${e.localizedMessage}"))
        }
    }

    /**
     * Tambah gejala baru
     */
    suspend fun insert(request: GejalaRequest): Result<Gejala> {
        return try {
            val result = client.postgrest.from(table)
                .insert(request) {
                    select()
                }
                .decodeSingle<Gejala>()
            Result.success(result)
        } catch (e: Exception) {
            val msg = if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                "Kode gejala '${request.kode}' sudah digunakan"
            } else {
                "Gagal menambah gejala: ${e.localizedMessage}"
            }
            Result.failure(Exception(msg))
        }
    }

    /**
     * Update gejala berdasarkan id
     */
    suspend fun update(id: Long, request: GejalaRequest): Result<Gejala> {
        return try {
            val result = client.postgrest.from(table)
                .update(request) {
                    select()
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Gejala>()
            Result.success(result)
        } catch (e: Exception) {
            val msg = if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                "Kode gejala '${request.kode}' sudah digunakan"
            } else {
                "Gagal mengupdate gejala: ${e.localizedMessage}"
            }
            Result.failure(Exception(msg))
        }
    }

    /**
     * Hapus gejala berdasarkan id
     */
    suspend fun delete(id: Long): Result<Boolean> {
        return try {
            client.postgrest.from(table)
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus gejala: ${e.localizedMessage}"))
        }
    }
}