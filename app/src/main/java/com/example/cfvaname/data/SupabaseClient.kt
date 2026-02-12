package com.example.cfvaname.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    val supabase = createSupabaseClient(
        supabaseUrl = "https://yushivrlfunsyhypvulp.supabase.co",
        supabaseKey = "sb_publishable_nbxGPJK7dy_qAIzPo7cDQA_Yzr4XOoM"
    ) {
        install(Postgrest)
    }
}