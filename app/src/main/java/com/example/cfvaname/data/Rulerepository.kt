package com.example.cfvaname.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class RuleRepository {

    private val client = SupabaseClient.supabase
    private val table = "rules"

    suspend fun getAll(): Result<List<Rule>> {
        return try {
            val result = client.postgrest.from(table)
                .select {
                    order("id", Order.ASCENDING)
                }
                .decodeList<Rule>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal memuat data rules: ${e.localizedMessage}"))
        }
    }

    suspend fun insert(request: RuleRequest): Result<Rule> {
        return try {
            val result = client.postgrest.from(table)
                .insert(request) {
                    select()
                }
                .decodeSingle<Rule>()
            Result.success(result)
        } catch (e: Exception) {
            val msg = if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                "Gejala tersebut sudah memiliki rule"
            } else {
                "Gagal menambah rule: ${e.localizedMessage}"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun update(id: Long, request: RuleRequest): Result<Rule> {
        return try {
            val result = client.postgrest.from(table)
                .update(request) {
                    select()
                    filter { eq("id", id) }
                }
                .decodeSingle<Rule>()
            Result.success(result)
        } catch (e: Exception) {
            val msg = if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                "Gejala tersebut sudah memiliki rule"
            } else {
                "Gagal mengupdate rule: ${e.localizedMessage}"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun delete(id: Long): Result<Boolean> {
        return try {
            client.postgrest.from(table).delete { filter { eq("id", id) } }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghapus rule: ${e.localizedMessage}"))
        }
    }
}