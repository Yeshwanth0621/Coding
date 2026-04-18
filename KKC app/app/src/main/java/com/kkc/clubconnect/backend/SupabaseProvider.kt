package com.kkc.clubconnect.backend

import android.content.Context
import com.kkc.clubconnect.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {

    @Volatile
    private var initialized = false

    @Volatile
    private var clientHolder: io.github.jan.supabase.SupabaseClient? = null

    fun ensureInitialized(context: Context): Boolean {
        if (initialized) {
            return true
        }

        if (!isConfigured()) {
            initialized = false
            return false
        }

        client(context)
        return initialized
    }

    fun isConfigured(): Boolean = try {
        BuildConfig.SUPABASE_URL.isNotBlank()
            && !BuildConfig.SUPABASE_URL.startsWith("CHANGE_ME")
            && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
            && !BuildConfig.SUPABASE_ANON_KEY.startsWith("CHANGE_ME")
    } catch (e: Exception) {
        false
    }

    fun client(context: Context): io.github.jan.supabase.SupabaseClient {
        clientHolder?.let { return it }

        synchronized(this) {
            clientHolder?.let { return it }

            val client = createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
            ) {
                install(Auth)
                install(Postgrest)
                install(Realtime)
                install(Storage)
            }

            clientHolder = client
            initialized = true
            return client
        }
    }
}
