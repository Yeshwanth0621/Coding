package com.kkc.clubconnect.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kkc.clubconnect.util.DateTimeUtils

class EventReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getString(keyEventId).orEmpty()
        val title = inputData.getString(keyTitle).orEmpty()
        val clubName = inputData.getString(keyClubName).orEmpty()
        val clubLogoUrl = inputData.getString(keyClubLogoUrl).orEmpty()
        val startAtMillis = inputData.getLong(keyStartAtMillis, 0L)
        val endAtMillis = inputData.getLong(keyEndAtMillis, 0L)
        val location = inputData.getString(keyLocation).orEmpty()

        if (eventId.isBlank() || title.isBlank() || startAtMillis <= 0L || endAtMillis <= 0L) {
            return Result.success()
        }

        val now = System.currentTimeMillis()
        if (endAtMillis <= now) {
            return Result.success()
        }

        NotificationHelper.showEventNotification(
            context = applicationContext,
            payload = EventNotificationPayload(
                id = eventId.hashCode(),
                title = "Starts in 1 hour",
                eventName = title,
                eventTimeLabel = DateTimeUtils.toRange(startAtMillis, endAtMillis),
                venue = location,
                clubName = clubName,
                clubLogoUrl = clubLogoUrl,
            ),
        )
        return Result.success()
    }

    companion object {
        const val keyEventId = "event_id"
        const val keyTitle = "event_title"
        const val keyClubName = "event_club_name"
        const val keyClubLogoUrl = "event_club_logo_url"
        const val keyStartAtMillis = "event_start_at_millis"
        const val keyEndAtMillis = "event_end_at_millis"
        const val keyLocation = "event_location"
    }
}
