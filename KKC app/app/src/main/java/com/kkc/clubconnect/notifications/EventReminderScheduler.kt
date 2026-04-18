package com.kkc.clubconnect.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kkc.clubconnect.model.ClubEvent
import java.util.concurrent.TimeUnit

object EventReminderScheduler {

    fun scheduleOneHourReminder(
        context: Context,
        event: ClubEvent,
    ) {
        val now = System.currentTimeMillis()
        if (event.endAtMillis <= now) {
            cancelOneHourReminder(context, event.id)
            return
        }

        val reminderAtMillis = (event.startAtMillis - oneHourMillis).coerceAtLeast(now)
        val delayMillis = (reminderAtMillis - now).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInputData(event.toReminderData())
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            reminderWorkName(event.id),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancelOneHourReminder(
        context: Context,
        eventId: String,
    ) {
        WorkManager.getInstance(context).cancelUniqueWork(reminderWorkName(eventId))
    }

    private fun reminderWorkName(eventId: String): String = "event_one_hour_reminder_$eventId"

    private fun ClubEvent.toReminderData(): Data = Data.Builder()
        .putString(EventReminderWorker.keyEventId, id)
        .putString(EventReminderWorker.keyTitle, title)
        .putString(EventReminderWorker.keyClubName, clubName)
        .putString(EventReminderWorker.keyClubLogoUrl, clubLogoUrl)
        .putLong(EventReminderWorker.keyStartAtMillis, startAtMillis)
        .putLong(EventReminderWorker.keyEndAtMillis, endAtMillis)
        .putString(EventReminderWorker.keyLocation, location)
        .build()

    private const val oneHourMillis = 60 * 60 * 1000L
}
