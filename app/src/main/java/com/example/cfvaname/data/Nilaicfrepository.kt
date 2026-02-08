package com.example.cfvaname.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject

/**
 * Repository CRUD untuk tabel nilai_cfs di Supabase.
 * Setelah insert/delete, memanggil recalculate_nilai_cf() agar
 * nilai terdistribusi rata dari 0.00 - 1.00.
 */
class NilaiCfRepository {

    private val client = SupabaseClient.supabase
    private val table = "nilai_cfs"

    /**
     * Ambil semua nilai CF diurutkan berdasarkan nilai ASC
     */
    suspend fun getAll(): Result<List<NilaiCf>> {
        return try {
            val result = client.postgrest.from(table)
                .select {
                    order("nilai", Order.ASCENDING)
                }
                .decodeList<NilaiCf>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data nilai CF: ${e.localizedMessage}"))
        }
    }

    /**
     * Tambah nilai CF baru, lalu recalculate semua nilai
     */
    suspend fun insert(keterangan: String): Result<Boolean> {
        return try {
            // Insert dengan nilai sementara 0
            val request = NilaiCfInsertRequest(keterangan = keterangan.trim(), nilai = 0.0)
            client.postgrest.from(table).insert(request)

            // Recalculate semua nilai agar rata
            recalculate()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menambah nilai CF: ${e.localizedMessage}"))
        }
    }

    /**
     * Update keterangan saja (nilai tetap diatur oleh recalculate)
     */
    suspend fun updateKeterangan(id: Long, keterangan: String): Result<Boolean> {
        return try {
            client.postgrest.from(table)
                .update(mapOf("keterangan" to keterangan.trim())) {
                    filter {
                        eq("id", id)
                    }
                }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mengupdate nilai CF: ${e.localizedMessage}"))
        }
    }

    /**
     * Hapus nilai CF, lalu recalculate semua nilai
     */
    suspend fun delete(id: Long): Result<Boolean> {
        return try {
            client.postgrest.from(table)
                .delete {
                    filter {
                        eq("id", id)
                    }
                }

            // Recalculate semua nilai agar rata
            recalculate()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus nilai CF: ${e.localizedMessage}"))
        }
    }

    /**
     * Panggil RPC function recalculate_nilai_cf() di Supabase
     * untuk mendistribusikan nilai secara merata 0.00 - 1.00
     */
    private suspend fun recalculate() {
        try {
            client.postgrest.rpc("recalculate_nilai_cf", buildJsonObject { })
        } catch (_: Exception) {
            // Silent: recalculate gagal tidak fatal
        }
    }
}