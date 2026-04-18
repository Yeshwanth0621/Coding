package com.kalamclub.booktopia.data

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository for all Booktopia data operations.
 */
object BooktopiaRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ==================== AUTH OPERATIONS ====================

    /**
     * Sign in with email and password.
     */
    suspend fun signIn(email: String, password: String): Result<UserInfo> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = SupabaseClient.auth.currentUserOrNull()
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign up with email, password, and username.
     */
    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        avatarUrl: String? = null
    ): Result<UserInfo> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.auth.signUp(Email) {
                this.email = email
                this.password = password
            }
            val user = SupabaseClient.auth.currentUserOrNull()
            if (user != null) {
                // Create user profile
                createUserProfile(user.id, username, avatarUrl)
                Result.success(user)
            } else {
                Result.failure(Exception("Sign up failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the current signed-in user.
     */
    fun getCurrentUser(): UserInfo? = SupabaseClient.auth.currentUserOrNull()

    /**
     * Check if a user is currently signed in.
     */
    fun isSignedIn(): Boolean = SupabaseClient.auth.currentUserOrNull() != null

    // ==================== PROFILE OPERATIONS ====================

    /**
     * Create a user profile.
     */
    suspend fun createUserProfile(
        userId: String,
        username: String,
        avatarUrl: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.postgrest.from("user_profiles").insert(
                UserProfileInsert(
                    userId = userId,
                    username = username,
                    avatarUrl = avatarUrl
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile by user ID.
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.postgrest.from("user_profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserProfile>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if a username is available.
     */
    suspend fun isUsernameAvailable(username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.postgrest.from("user_profiles")
                .select(columns = Columns.list("id")) {
                    filter {
                        ilike("username", username)
                    }
                }
                .decodeList<Map<String, String>>()
            result.isEmpty()
        } catch (e: Exception) {
            true // Assume available on error
        }
    }

    // ==================== READING OPERATIONS ====================

    /**
     * Get today's date string.
     */
    fun getTodayDateString(): String = LocalDate.now().format(dateFormatter)

    /**
     * Get allowed dates for logging (today and past 2 days).
     */
    fun getAllowedDates(): List<DateOption> {
        val today = LocalDate.now()
        return listOf(
            DateOption("Today", today.format(dateFormatter), true),
            DateOption("Yesterday", today.minusDays(1).format(dateFormatter)),
            DateOption("2 days ago", today.minusDays(2).format(dateFormatter))
        )
    }

    /**
     * Submit or update a reading entry.
     */
    suspend fun submitReading(
        userId: String,
        email: String,
        pagesRead: Int,
        readingDate: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Check if entry exists for this date
            val existing = getReadingForDate(userId, readingDate).getOrNull()

            if (existing != null) {
                // Update existing entry
                SupabaseClient.postgrest.from("daily_readings")
                    .update(DailyReadingUpdate(pagesRead)) {
                        filter {
                            eq("id", existing.id)
                        }
                    }
                Result.success(true) // isUpdate = true
            } else {
                // Insert new entry
                SupabaseClient.postgrest.from("daily_readings").insert(
                    DailyReadingInsert(
                        userId = userId,
                        email = email,
                        pagesRead = pagesRead,
                        readingDate = readingDate
                    )
                )
                Result.success(false) // isUpdate = false
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get reading for a specific date.
     */
    suspend fun getReadingForDate(userId: String, date: String): Result<DailyReading?> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.postgrest.from("daily_readings")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("reading_date", date)
                    }
                }
                .decodeSingleOrNull<DailyReading>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get today's reading for a user.
     */
    suspend fun getTodayReading(userId: String): Result<DailyReading?> =
        getReadingForDate(userId, getTodayDateString())

    /**
     * Get all readings for a user.
     */
    suspend fun getUserReadings(userId: String): Result<List<DailyReading>> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.postgrest.from("daily_readings")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("reading_date", Order.DESCENDING)
                }
                .decodeList<DailyReading>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get total pages read by a user.
     */
    suspend fun getUserTotalPages(userId: String): Int {
        val readings = getUserReadings(userId).getOrNull() ?: emptyList()
        return readings.sumOf { it.pagesRead }
    }

    /**
     * Get the leaderboard (top 20 readers).
     */
    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> = withContext(Dispatchers.IO) {
        try {
            val readings = SupabaseClient.postgrest.from("daily_readings")
                .select(columns = Columns.list("email", "pages_read"))
                .decodeList<Map<String, Any>>()

            // Aggregate pages by email
            val aggregated = readings
                .groupBy { it["email"] as String }
                .map { (email, entries) ->
                    val totalPages = entries.sumOf {
                        (it["pages_read"] as? Number)?.toInt() ?: 0
                    }
                    LeaderboardEntry(
                        email = email,
                        totalPages = totalPages
                    )
                }
                .sortedByDescending { it.totalPages }
                .take(20)
                .mapIndexed { index, entry ->
                    entry.copy(rank = index + 1)
                }

            Result.success(aggregated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
