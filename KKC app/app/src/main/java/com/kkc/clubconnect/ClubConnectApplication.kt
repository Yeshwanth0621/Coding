package com.kkc.clubconnect

import android.app.Application
import com.kkc.clubconnect.backend.SupabaseProvider
import com.kkc.clubconnect.notifications.NotificationScheduler
import com.kkc.clubconnect.notifications.NotificationHelper

class ClubConnectApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        val backendConfigured = SupabaseProvider.ensureInitialized(this)
        NotificationHelper.createChannel(this)
        if (backendConfigured) {
            NotificationScheduler.schedule(this)
        }

        appContainer = AppContainer(backendConfigured = backendConfigured)
    }
}
