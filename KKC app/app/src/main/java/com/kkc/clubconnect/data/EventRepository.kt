package com.kkc.clubconnect.data

import android.content.Context
import com.kkc.clubconnect.backend.SupabaseProvider
import com.kkc.clubconnect.model.ClubEvent
import com.kkc.clubconnect.model.EventDraft
import com.kkc.clubconnect.util.ClubBrandingUtils
import com.kkc.clubconnect.util.UrlUtils
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class EventRepository(
    private val backendConfigured: Boolean,
) {

    private fun client(context: Context) = SupabaseProvider.client(context)

    @OptIn(SupabaseExperimental::class)
    fun observeEvents(context: Context): Flow<List<ClubEvent>> {
        if (!backendConfigured) {
            return flowOf(emptyList())
        }

        return client(context).from(eventsTable)
            .selectAsFlow(ClubEvent::id)
            .map { events ->
                events.sortedWith(
                    compareByDescending<ClubEvent> { it.featured }
                        .thenBy { it.startAtMillis },
                )
            }
    }

    suspend fun createEvent(
        context: Context,
        draft: EventDraft,
        session: ClubSession,
    ): Result<Unit> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        if (!session.hasCompletedSetup || session.uid.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Finish the club setup before posting events."))
        }

        return runCatching {
            client(context)
                .from(eventsTable)
                .insert(draft.toInsertPayload(session, System.currentTimeMillis()))
        }
    }

    suspend fun updateEvent(
        context: Context,
        eventId: String,
        draft: EventDraft,
        session: ClubSession,
    ): Result<Unit> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        if (!session.hasCompletedSetup || session.uid.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Finish the club setup before editing events."))
        }

        return runCatching {
            val timestamp = System.currentTimeMillis()
            client(context)
                .from(eventsTable)
                .update(
                    {
                        set("club_id", session.clubId)
                        set("club_name", session.clubName)
                        set("club_logo_url", session.clubLogoUrl)
                        set("club_banner_color_hex", ClubBrandingUtils.normalizeBannerColorHex(session.clubBannerColorHex))
                        set("title", draft.title.trim())
                        set("summary", draft.summary.trim())
                        set("description", draft.description.trim().ifBlank { draft.summary.trim() })
                        set("location", draft.location.trim())
                        set("image_url", draft.imageUrl.trim())
                        set("registration_link", UrlUtils.normalizeRegistrationUrl(draft.registrationLink))
                        set("notify_one_hour_before", draft.notifyOneHourBefore)
                        set("start_at_millis", draft.startAtMillis)
                        set("end_at_millis", draft.endAtMillis)
                        set("featured", draft.featured)
                        set("updated_at_millis", timestamp)
                        set("author_email", session.contactEmail)
                    },
                ) {
                    filter {
                        eq("id", eventId)
                        eq("club_owner_id", session.uid)
                    }
                }
        }
    }

    suspend fun deleteEvent(
        context: Context,
        eventId: String,
        session: ClubSession,
    ): Result<Unit> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        if (!session.hasCompletedSetup || session.uid.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Finish the club setup before deleting events."))
        }

        return runCatching {
            client(context)
                .from(eventsTable)
                .delete {
                    filter {
                        eq("id", eventId)
                        eq("club_owner_id", session.uid)
                    }
                }
        }
    }

    suspend fun fetchUpdatedEventsSince(
        context: Context,
        timestamp: Long,
    ): Result<List<ClubEvent>> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        return runCatching {
            client(context)
                .from(eventsTable)
                .select {
                    filter {
                        gt("updated_at_millis", timestamp)
                    }
                }
                .decodeList<ClubEvent>()
                .sortedByDescending { it.updatedAtMillis }
        }
    }

    suspend fun fetchUpcomingEvents(context: Context): Result<List<ClubEvent>> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        val now = System.currentTimeMillis()
        return runCatching {
            client(context)
                .from(eventsTable)
                .select {
                    filter {
                        gte("end_at_millis", now)
                    }
                }
                .decodeList<ClubEvent>()
                .sortedBy { it.startAtMillis }
        }
    }

    suspend fun fetchExpiredOwnEvents(
        context: Context,
        userId: String,
        cutoffMillis: Long = System.currentTimeMillis(),
    ): Result<List<ClubEvent>> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        return runCatching {
            client(context)
                .from(eventsTable)
                .select {
                    filter {
                        eq("club_owner_id", userId)
                        lt("end_at_millis", cutoffMillis)
                    }
                }
                .decodeList<ClubEvent>()
                .sortedBy { it.endAtMillis }
        }
    }

    suspend fun fetchExpiredEvents(
        context: Context,
        cutoffMillis: Long = System.currentTimeMillis(),
    ): Result<List<ClubEvent>> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        return runCatching {
            client(context)
                .from(eventsTable)
                .select {
                    filter {
                        lt("end_at_millis", cutoffMillis)
                    }
                }
                .decodeList<ClubEvent>()
                .sortedBy { it.endAtMillis }
        }
    }

    suspend fun deleteExpiredEvent(
        context: Context,
        eventId: String,
        cutoffMillis: Long = System.currentTimeMillis(),
    ): Result<Unit> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        return runCatching {
            client(context)
                .from(eventsTable)
                .delete {
                    filter {
                        eq("id", eventId)
                        lt("end_at_millis", cutoffMillis)
                    }
                }
        }
    }

    private companion object {
        const val eventsTable = "events"
    }
}
