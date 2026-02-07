package com.example.cfvaname.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

/**
 * Repository CRUD untuk tabel hipotesiss di Supabase
 */
class HipotesisRepository {

    private val client = SupabaseClient.supabase
    private val table = "hipotesiss"

    /**
     * Ambil semua hipotesis, diurutkan berdasarkan kode
     */
    suspend fun getAll(): Result<List<Hipotesis>> {
        return try {
            val result = client.postgrest.from(table)
                .select {
                    order("kode", Order.ASCENDING)
                }
                .decodeList<Hipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data hipotesis: ${e.localizedMessage}"))
        }
    }

    /**
     * Cari hipotesis berdasarkan keyword (kode atau nama)
     */
    suspend fun search(keyword: String): Result<List<Hipotesis>> {
        return try {
            val result = client.postgrest.from(table)
                .select {
                    filter {
                        or {
                            Hipotesis::kode ilike "%$keyword%"
                            Hipotesis::nama ilike "%$keyword%"
                        }
                    }
                    order("kode", Order.ASCENDING)
                }
                .decodeList<Hipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mencari hipotesis: ${e.localizedMessage}"))
        }
    }

    /**
     * Tambah hipotesis baru
     */
    suspend fun insert(request: HipotesisRequest): Result<Hipotesis> {
        return try {
            val result = client.postgrest.from(table)
                .insert(request) {
                    select()
                }
                .decodeSingle<Hipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            val msg = if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                "Kode hipotesis '${request.kode}' sudah digunakan"
            } else {
                "Gagal menambah hipotesis: ${e.localizedMessage}"
            }
            Result.failure(Exception(msg))
        }
    }

    /**
     * Update hipotesis berdasarkan id
     */
    suspend fun update(id: Long, request: HipotesisRequest): Result<Hipotesis> {
        return try {
            val result = client.postgrest.from(table)
                .update(request) {
                    select()
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Hipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            val msg = if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                "Kode hipotesis '${request.kode}' sudah digunakan"
            } else {
                "Gagal mengupdate hipotesis: ${e.localizedMessage}"
            }
            Result.failure(Exception(msg))
        }
    }

    /**
     * Hapus hipotesis berdasarkan id
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
            Result.failure(Exception("Gagal menghapus hipotesis: ${e.localizedMessage}"))
        }
    }
}