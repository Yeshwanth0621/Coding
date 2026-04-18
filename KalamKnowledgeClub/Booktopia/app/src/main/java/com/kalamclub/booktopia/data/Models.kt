package com.kalamclub.booktopia.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a daily reading entry in the database.
 */
@Serializable
data class DailyReading(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val email: String = "",
    @SerialName("pages_read")
    val pagesRead: Int = 0,
    @SerialName("reading_date")
    val readingDate: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

/**
 * Represents a user profile in the database.
 */
@Serializable
data class UserProfile(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val username: String = "",
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

/**
 * Represents a leaderboard entry (aggregated data).
 */
data class LeaderboardEntry(
    val email: String,
    val username: String? = null,
    val avatarUrl: String? = null,
    val totalPages: Int,
    val rank: Int = 0
)

/**
 * Request model for inserting a daily reading.
 */
@Serializable
data class DailyReadingInsert(
    @SerialName("user_id")
    val userId: String,
    val email: String,
    @SerialName("pages_read")
    val pagesRead: Int,
    @SerialName("reading_date")
    val readingDate: String
)

/**
 * Request model for updating a daily reading.
 */
@Serializable
data class DailyReadingUpdate(
    @SerialName("pages_read")
    val pagesRead: Int
)

/**
 * Request model for creating a user profile.
 */
@Serializable
data class UserProfileInsert(
    @SerialName("user_id")
    val userId: String,
    val username: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

/**
 * Allowed date options for logging readings.
 */
data class DateOption(
    val label: String,
    val value: String, // Format: YYYY-MM-DD
    val isToday: Boolean = false
)
