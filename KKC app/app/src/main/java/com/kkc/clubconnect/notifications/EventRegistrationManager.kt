package com.kkc.clubconnect.notifications

import android.content.Context
import com.kkc.clubconnect.model.ClubEvent

object EventRegistrationManager {

    fun isRegistered(
        context: Context,
        eventId: String,
    ): Boolean {
        val ids = preferenceStore(context).getStringSet(registeredEventIdsKey, emptySet()).orEmpty()
        return ids.contains(eventId)
    }

    fun isOneHourReminderEnabled(
        context: Context,
        eventId: String,
    ): Boolean = preferenceStore(context).getBoolean(reminderEnabledKey(eventId), true)

    fun registerForEvent(
        context: Context,
        event: ClubEvent,
        oneHourReminderEnabled: Boolean,
    ) {
        val prefs = preferenceStore(context)
        val ids = prefs.getStringSet(registeredEventIdsKey, emptySet()).orEmpty().toMutableSet()
        ids.add(event.id)
        prefs.edit()
            .putStringSet(registeredEventIdsKey, ids)
            .putBoolean(reminderEnabledKey(event.id), oneHourReminderEnabled)
            .apply()

        if (oneHourReminderEnabled) {
            EventReminderScheduler.scheduleOneHourReminder(context, event)
        } else {
            EventReminderScheduler.cancelOneHourReminder(context, event.id)
        }
    }

    fun syncRegisteredReminders(
        context: Context,
        events: List<ClubEvent>,
    ) {
        val now = System.currentTimeMillis()
        val eventsById = events.associateBy { it.id }
        val prefs = preferenceStore(context)
        val registeredIds = prefs.getStringSet(registeredEventIdsKey, emptySet()).orEmpty().toMutableSet()
        var changed = false

        registeredIds.toList().forEach { eventId ->
            val event = eventsById[eventId]
            if (event == null || event.endAtMillis <= now) {
                EventReminderScheduler.cancelOneHourReminder(context, eventId)
                registeredIds.remove(eventId)
                changed = true
                return@forEach
            }

            val reminderEnabled = prefs.getBoolean(reminderEnabledKey(eventId), true)
            if (reminderEnabled) {
                EventReminderScheduler.scheduleOneHourReminder(context, event)
            } else {
                EventReminderScheduler.cancelOneHourReminder(context, eventId)
            }
        }

        if (changed) {
            prefs.edit().putStringSet(registeredEventIdsKey, registeredIds).apply()
        }
    }

    private fun preferenceStore(context: Context) =
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    private fun reminderEnabledKey(eventId: String): String = "one_hour_enabled_$eventId"

    private const val preferencesName = "club_event_registrations"
    private const val registeredEventIdsKey = "registered_event_ids"
}
