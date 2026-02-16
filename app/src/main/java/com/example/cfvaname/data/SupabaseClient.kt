package com.example.cfvaname.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    val supabase = createSupabaseClient(
        supabaseUrl = "https://hyiusidrhovxcxeuknpa.supabase.co",
        supabaseKey = "sb_publishable_utQDkobEKUwGs02qBkU7dg_KrkzMsbJ"
    ) {
        install(Postgrest)
    }
}