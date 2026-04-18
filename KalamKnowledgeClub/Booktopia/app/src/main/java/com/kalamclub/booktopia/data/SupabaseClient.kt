package com.kalamclub.booktopia.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseClient {

    private const val SUPABASE_URL = "https://odmdtepogrkyihhhyfbj.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9kbWR0ZXBvZ3JreWloaGh5ZmJqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc4MDI1NTIsImV4cCI6MjA4MzM3ODU1Mn0.Nu55J-UZ95Jy3hOVdxUXrGMI-MAtVSDJ1NleOqrXqAs"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        anonKey = SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
        install(Storage)
    }

    val auth: GoTrue
        get() = client.gotrue

    val postgrest: Postgrest
        get() = client.postgrest

    val storage: Storage
        get() = client.storage
}
