package com.kkc.clubconnect.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClubEvent(
    @SerialName("id")
    val id: String = "",
    @SerialName("club_owner_id")
    val clubOwnerId: String = "",
    @SerialName("club_id")
    val clubId: String = "",
    @SerialName("club_name")
    val clubName: String = "",
    @SerialName("club_logo_url")
    val clubLogoUrl: String = "",
    @SerialName("club_banner_color_hex")
    val clubBannerColorHex: String = "#0EA5E9",
    @SerialName("title")
    val title: String = "",
    @SerialName("summary")
    val summary: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("location")
    val location: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    @SerialName("registration_link")
    val registrationLink: String = "",
    @SerialName("notify_one_hour_before")
    val notifyOneHourBefore: Boolean = true,
    @SerialName("start_at_millis")
    val startAtMillis: Long = 0L,
    @SerialName("end_at_millis")
    val endAtMillis: Long = 0L,
    @SerialName("featured")
    val featured: Boolean = false,
    @SerialName("created_at_millis")
    val createdAtMillis: Long = 0L,
    @SerialName("updated_at_millis")
    val updatedAtMillis: Long = 0L,
    @SerialName("author_email")
    val authorEmail: String? = null,
)

@Serializable
data class ClubProfile(
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("club_id")
    val clubId: String = "",
    @SerialName("club_name")
    val clubName: String = "",
    @SerialName("club_logo_url")
    val clubLogoUrl: String = "",
    @SerialName("club_banner_color_hex")
    val clubBannerColorHex: String = "#0EA5E9",
    @SerialName("contact_name")
    val contactName: String = "",
    @SerialName("contact_email")
    val contactEmail: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("updated_at_millis")
    val updatedAtMillis: Long = 0L,
)

data class ClubProfileDraft(
    val clubId: String = "",
    val clubName: String = "",
    val clubLogoUrl: String = "",
    val clubBannerColorHex: String = "#0EA5E9",
    val contactName: String = "",
    val contactEmail: String = "",
    val description: String = "",
)

@Serializable
data class EventInsertPayload(
    @SerialName("club_owner_id")
    val clubOwnerId: String,
    @SerialName("club_id")
    val clubId: String,
    @SerialName("club_name")
    val clubName: String,
    @SerialName("club_logo_url")
    val clubLogoUrl: String,
    @SerialName("club_banner_color_hex")
    val clubBannerColorHex: String,
    @SerialName("title")
    val title: String,
    @SerialName("summary")
    val summary: String,
    @SerialName("description")
    val description: String,
    @SerialName("location")
    val location: String,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("registration_link")
    val registrationLink: String,
    @SerialName("notify_one_hour_before")
    val notifyOneHourBefore: Boolean,
    @SerialName("start_at_millis")
    val startAtMillis: Long,
    @SerialName("end_at_millis")
    val endAtMillis: Long,
    @SerialName("featured")
    val featured: Boolean,
    @SerialName("created_at_millis")
    val createdAtMillis: Long,
    @SerialName("updated_at_millis")
    val updatedAtMillis: Long,
    @SerialName("author_email")
    val authorEmail: String,
)
