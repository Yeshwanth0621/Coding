package com.kkc.clubconnect.model

import com.kkc.clubconnect.data.ClubSession
import com.kkc.clubconnect.util.UrlUtils
import java.time.Instant
import java.time.temporal.ChronoUnit

data class EventDraft(
    val title: String = "",
    val summary: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val registrationLink: String = "",
    val notifyOneHourBefore: Boolean = true,
    val startAtMillis: Long = defaultStartTime(),
    val endAtMillis: Long = defaultStartTime() + 2 * 60 * 60 * 1000,
    val featured: Boolean = false,
) {
    fun toInsertPayload(
        session: ClubSession,
        timestamp: Long,
    ): EventInsertPayload = EventInsertPayload(
        clubOwnerId = session.uid.orEmpty(),
        clubId = session.clubId,
        clubName = session.clubName,
        clubLogoUrl = session.clubLogoUrl,
        clubBannerColorHex = session.clubBannerColorHex,
        title = title.trim(),
        summary = summary.trim(),
        description = description.trim().ifBlank { summary.trim() },
        location = location.trim(),
        imageUrl = imageUrl.trim(),
        registrationLink = UrlUtils.normalizeRegistrationUrl(registrationLink),
        notifyOneHourBefore = notifyOneHourBefore,
        startAtMillis = startAtMillis,
        endAtMillis = endAtMillis,
        featured = featured,
        createdAtMillis = timestamp,
        updatedAtMillis = timestamp,
        authorEmail = session.contactEmail,
    )

    companion object {
        fun fromEvent(event: ClubEvent): EventDraft = EventDraft(
            title = event.title,
            summary = event.summary,
            description = event.description,
            location = event.location,
            imageUrl = event.imageUrl,
            registrationLink = if (UrlUtils.isDummyRegistrationUrl(event.registrationLink)) {
                ""
            } else {
                event.registrationLink
            },
            notifyOneHourBefore = event.notifyOneHourBefore,
            startAtMillis = event.startAtMillis,
            endAtMillis = event.endAtMillis,
            featured = event.featured,
        )

        private fun defaultStartTime(): Long = Instant.now()
            .plus(1, ChronoUnit.DAYS)
            .truncatedTo(ChronoUnit.HOURS)
            .toEpochMilli()
    }
}
