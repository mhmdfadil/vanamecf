package com.example.cfvaname.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

/**
 * Repository untuk tabel kuesioners dan kuesioner_data di Supabase
 * ✅ PERBAIKAN: countKuesionerData() sekarang menghitung gejala UNIK (bukan total entries)
 */
class KuesionerRepository {

    private val client = SupabaseClient.supabase

    // === KUESIONERS ===

    suspend fun getAllKuesioner(): Result<List<Kuesioner>> {
        return try {
            val result = client.postgrest.from("kuesioners")
                .select { order("created_at", Order.DESCENDING) }
                .decodeList<Kuesioner>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat kuesioner: ${e.localizedMessage}"))
        }
    }

    suspend fun insertKuesioner(request: KuesionerInsertRequest): Result<Kuesioner> {
        return try {
            val result = client.postgrest.from("kuesioners")
                .insert(request) { select() }
                .decodeSingle<Kuesioner>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menyimpan kuesioner: ${e.localizedMessage}"))
        }
    }

    suspend fun deleteKuesioner(id: Long): Result<Boolean> {
        return try {
            // kuesioner_data will cascade delete
            client.postgrest.from("kuesioners").delete { filter { eq("id", id) } }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus kuesioner: ${e.localizedMessage}"))
        }
    }

    // === KUESIONER DATA ===

    suspend fun insertKuesionerData(dataList: List<KuesionerDataInsertRequest>): Result<Boolean> {
        return try {
            client.postgrest.from("kuesioner_data").insert(dataList)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menyimpan data gejala: ${e.localizedMessage}"))
        }
    }

    suspend fun getKuesionerData(kuesionerId: Long): Result<List<KuesionerData>> {
        return try {
            val result = client.postgrest.from("kuesioner_data")
                .select { filter { eq("kuesioner_id", kuesionerId) } }
                .decodeList<KuesionerData>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data kuesioner: ${e.localizedMessage}"))
        }
    }

    /**
     * ✅ PERBAIKAN: Hitung GEJALA UNIK yang dipilih
     * 
     * Sebelumnya:
     *   - 30 gejala → 39 entries di kuesioner_data (duplikasi)
     *   - countKuesionerData() return 39
     * 
     * Sekarang:
     *   - 30 gejala → 30 entries di kuesioner_data (representative saja)
     *   - countKuesionerData() return 30 (akurat!)
     * 
     * Jika ada old data dengan duplikasi, method ini akan count unique gejalaId
     * sehingga tetap menampilkan 30 (bukan 39)
     */
    suspend fun countKuesionerData(kuesionerId: Long): Int {
        return try {
            val result = client.postgrest.from("kuesioner_data")
                .select { filter { eq("kuesioner_id", kuesionerId) } }
                .decodeList<KuesionerData>()
            
            // ✅ PERBAIKAN: Hitung unique gejala_hipotesis_id
            // Untuk old data: jika ada duplikasi, kita map ke gejalaId yang unik
            val ghMap = getAllGejalaHipotesis().getOrDefault(emptyList())
                .associateBy { it.id }
            
            val uniqueGejalaIds = result.mapNotNull { data ->
                // Get gejalaHipotesis dari id
                ghMap[data.gejalaHipotesisId]?.gejalaId
            }.toSet() // Convert to Set untuk unique
            
            uniqueGejalaIds.size
        } catch (e: Exception) {
            0
        }
    }

    // === LOAD ALL RELATED DATA FOR CF CALCULATION ===

    suspend fun getAllGejalas(): Result<List<Gejala>> {
        return try {
            val result = client.postgrest.from("gejalas")
                .select { order("kode", Order.ASCENDING) }
                .decodeList<Gejala>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat gejala: ${e.localizedMessage}"))
        }
    }

    suspend fun getAllHipotesis(): Result<List<Hipotesis>> {
        return try {
            val result = client.postgrest.from("hipotesiss")
                .select { order("kode", Order.ASCENDING) }
                .decodeList<Hipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat hipotesis: ${e.localizedMessage}"))
        }
    }

    suspend fun getAllGejalaHipotesis(): Result<List<GejalaHipotesis>> {
        return try {
            val result = client.postgrest.from("gejala_hipotesis")
                .select { order("id", Order.ASCENDING) }
                .decodeList<GejalaHipotesis>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat gejala-hipotesis: ${e.localizedMessage}"))
        }
    }

    suspend fun getAllRules(): Result<List<Rule>> {
        return try {
            val result = client.postgrest.from("rules").select().decodeList<Rule>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat rules: ${e.localizedMessage}"))
        }
    }

    suspend fun getAllNilaiCf(): Result<List<NilaiCf>> {
        return try {
            val result = client.postgrest.from("nilai_cfs")
                .select { order("nilai", Order.ASCENDING) }
                .decodeList<NilaiCf>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat nilai CF: ${e.localizedMessage}"))
        }
    }
} 