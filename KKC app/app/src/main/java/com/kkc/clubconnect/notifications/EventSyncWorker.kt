package com.kkc.clubconnect.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kkc.clubconnect.backend.SupabaseProvider
import com.kkc.clubconnect.data.EventRepository
import com.kkc.clubconnect.data.MediaRepository
import com.kkc.clubconnect.model.ClubEvent
import com.kkc.clubconnect.util.DateTimeUtils

class EventSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    private val repository = EventRepository(backendConfigured = SupabaseProvider.isConfigured())
    private val mediaRepository = MediaRepository(backendConfigured = SupabaseProvider.isConfigured())
    private val prefs = appContext.getSharedPreferences("club_event_worker", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        if (!SupabaseProvider.ensureInitialized(applicationContext)) {
            NotificationScheduler.scheduleNextBackgroundSync(applicationContext)
            return Result.success()
        }

        val now = System.currentTimeMillis()
        val lastSync = prefs.getLong(lastSyncKey, -1L)
        cleanupExpiredEvents(now)
        val upcomingEvents = repository.fetchUpcomingEvents(applicationContext).getOrElse {
            NotificationScheduler.scheduleNextBackgroundSync(applicationContext)
            return Result.success()
        }
        EventRegistrationManager.syncRegisteredReminders(
            context = applicationContext,
            events = upcomingEvents,
        )

        if (lastSync == -1L) {
            prefs.edit().putLong(lastSyncKey, now).apply()
            NotificationScheduler.scheduleNextBackgroundSync(applicationContext)
            return Result.success()
        }

        val changedEvents = repository.fetchUpdatedEventsSince(applicationContext, lastSync).getOrElse {
            NotificationScheduler.scheduleNextBackgroundSync(applicationContext)
            return Result.success()
        }.filter { it.endAtMillis >= now - staleWindowMillis }

        maybeNotifyAboutUpdates(changedEvents)

        prefs.edit().putLong(lastSyncKey, now).apply()
        NotificationScheduler.scheduleNextBackgroundSync(applicationContext)
        return Result.success()
    }

    private suspend fun cleanupExpiredEvents(now: Long) {
        val expiredEvents = repository.fetchExpiredEvents(
            context = applicationContext,
            cutoffMillis = now,
        ).getOrDefault(emptyList())

        expiredEvents.forEach { event ->
            if (event.imageUrl.isNotBlank()) {
                val imageCleanup = mediaRepository.deleteManagedAssetFromUrl(
                    context = applicationContext,
                    assetUrl = event.imageUrl,
                )
                if (imageCleanup.isFailure) {
                    return@forEach
                }
            }

            repository.deleteExpiredEvent(
                context = applicationContext,
                eventId = event.id,
                cutoffMillis = now,
            )
        }
    }

    private suspend fun maybeNotifyAboutUpdates(changedEvents: List<ClubEvent>) {
        if (changedEvents.isEmpty()) {
            return
        }

        if (changedEvents.size == 1) {
            val event = changedEvents.first()
            NotificationHelper.showEventNotification(
                context = applicationContext,
                payload = EventNotificationPayload(
                    id = event.id.hashCode(),
                    title = "Event update",
                    eventName = event.title,
                    eventTimeLabel = DateTimeUtils.toRange(event.startAtMillis, event.endAtMillis),
                    venue = event.location,
                    clubName = event.clubName,
                    bodyOverride = buildString {
                        append(
                            event.summary.ifBlank {
                                "Event details were updated."
                            },
                        )
                        append('\n')
                        append(DateTimeUtils.toRange(event.startAtMillis, event.endAtMillis))
                        if (event.location.isNotBlank()) {
                            append('\n')
                            append("Venue: ")
                            append(event.location)
                        }
                    },
                    clubLogoUrl = event.clubLogoUrl,
                ),
            )
            return
        }

        NotificationHelper.showEventNotification(
            context = applicationContext,
            title = "${changedEvents.size} new club updates",
            body = "Fresh events were posted or updated. Open CIT Club Connect for the latest campus schedule.",
        )
    }

    private companion object {
        const val lastSyncKey = "last_sync_millis"
        const val staleWindowMillis = 12 * 60 * 60 * 1000L
    }
}
