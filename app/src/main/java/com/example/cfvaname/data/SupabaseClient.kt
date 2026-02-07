package com.example.cfvaname.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    val supabase = createSupabaseClient(
        supabaseUrl = "https://wzqiqrcgyfprfozhveow.supabase.co",
        supabaseKey = "sb_publishable_hotfaR7elBbYBvMIcl2yEQ_hVPvfkAw"
    ) {
        install(Postgrest)
    }
}