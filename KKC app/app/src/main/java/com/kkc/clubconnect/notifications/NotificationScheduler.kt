package com.kkc.clubconnect.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.NetworkType
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val backgroundSyncWorkName = "club_event_sync_5_min"
    private const val immediateSyncWorkName = "club_event_sync_bootstrap"
    private const val legacyPeriodicWorkName = "club_event_sync"

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        clearStaleWorkerCache(context)

        // Remove previous scheduler strategy so only one background sync path remains active.
        workManager.cancelUniqueWork(legacyPeriodicWorkName)
        workManager.cancelUniqueWork(backgroundSyncWorkName)

        val immediateRequest = OneTimeWorkRequestBuilder<EventSyncWorker>()
            .setConstraints(defaultConstraints())
            .build()

        workManager.enqueueUniqueWork(
            immediateSyncWorkName,
            ExistingWorkPolicy.REPLACE,
            immediateRequest,
        )

        scheduleNextBackgroundSync(context, delayMinutes = backgroundRefreshMinutes)
    }

    fun scheduleNextBackgroundSync(
        context: Context,
        delayMinutes: Long = backgroundRefreshMinutes,
    ) {
        val request = OneTimeWorkRequestBuilder<EventSyncWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(defaultConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            backgroundSyncWorkName,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun defaultConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private fun clearStaleWorkerCache(context: Context) {
        val prefs = context.getSharedPreferences(workerPrefsName, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val lastSync = prefs.getLong(lastSyncKey, -1L)
        val looksStale = lastSync > now || (lastSync != -1L && now - lastSync > staleCacheWindowMillis)
        if (looksStale) {
            prefs.edit().clear().apply()
        }
    }

    private const val backgroundRefreshMinutes = 5L
    private const val workerPrefsName = "club_event_worker"
    private const val lastSyncKey = "last_sync_millis"
    private const val staleCacheWindowMillis = 3L * 24L * 60L * 60L * 1000L
}
