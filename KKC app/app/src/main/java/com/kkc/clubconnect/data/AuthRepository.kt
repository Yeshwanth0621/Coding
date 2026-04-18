package com.kkc.clubconnect.data

import android.content.Context
import com.kkc.clubconnect.backend.SupabaseProvider
import com.kkc.clubconnect.model.ClubProfile
import com.kkc.clubconnect.model.ClubProfileDraft
import com.kkc.clubconnect.util.ClubBrandingUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest

data class ClubSession(
    val uid: String? = null,
    val clubId: String = "",
    val clubName: String = "",
    val clubLogoUrl: String = "",
    val clubBannerColorHex: String = ClubBrandingUtils.defaultBannerColorHex,
    val contactName: String = "",
    val contactEmail: String = "",
    val description: String = "",
    val hasCompletedSetup: Boolean = false,
) {
    val isSignedIn: Boolean
        get() = uid != null
}

class AuthRepository(
    private val backendConfigured: Boolean,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSession(context: Context): Flow<ClubSession> {
        if (!backendConfigured) {
            return flowOf(ClubSession())
        }

        val client = SupabaseProvider.client(context)
        return client.auth.sessionStatus.mapLatest {
            val session = client.auth.currentSessionOrNull() ?: return@mapLatest ClubSession()
            val user = session.user ?: runCatching {
                client.auth.retrieveUserForCurrentSession(updateSession = true)
            }.getOrNull() ?: return@mapLatest ClubSession()

            val profile = fetchOwnClubProfile(context, user.id)
            if (profile == null) {
                return@mapLatest ClubSession(
                    uid = user.id,
                    clubId = extractClubId(user.email.orEmpty()),
                    contactEmail = "",
                    hasCompletedSetup = false,
                )
            }

            ClubSession(
                uid = user.id,
                clubId = profile.clubId,
                clubName = profile.clubName,
                clubLogoUrl = profile.clubLogoUrl,
                clubBannerColorHex = ClubBrandingUtils.normalizeBannerColorHex(profile.clubBannerColorHex),
                contactName = profile.contactName,
                contactEmail = profile.contactEmail,
                description = profile.description,
                hasCompletedSetup = true,
            )
        }
    }

    suspend fun signIn(
        context: Context,
        clubId: String,
        password: String,
    ): Result<Unit> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        val normalizedClubId = normalizeClubId(clubId)
        if (normalizedClubId.isBlank()) {
            return Result.failure(IllegalArgumentException("Enter a valid club ID."))
        }

        val authEmail = fetchClubProfileByClubId(context, normalizedClubId)?.contactEmail
            ?.trim()
            .orEmpty()
        if (authEmail.isBlank()) {
            return Result.failure(
                IllegalStateException(
                    "This club ID has not been set up yet. Use First setup to create the club access.",
                ),
            )
        }

        return runCatching {
            SupabaseProvider.client(context).auth.signInWith(Email) {
                email = authEmail
                this.password = password
            }
        }.recoverCatching { error ->
            throw IllegalStateException(error.message ?: "Club sign-in failed.")
        }
    }

    suspend fun registerClub(
        context: Context,
        password: String,
        draft: ClubProfileDraft,
    ): Result<Unit> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        val normalizedClubId = normalizeClubId(draft.clubId)
        val sanitizedDraft = draft.copy(
            clubId = normalizedClubId,
            contactEmail = draft.contactEmail.trim(),
            clubBannerColorHex = ClubBrandingUtils.normalizeBannerColorHex(draft.clubBannerColorHex),
        )
        val validationError = validateClubDraft(sanitizedDraft, password)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        return runCatching {
            val client = SupabaseProvider.client(context)
            client.auth.signUpWith(Email) {
                email = sanitizedDraft.contactEmail
                this.password = password
            }

            val user = client.auth.currentSessionOrNull()?.user
                ?: throw IllegalStateException(
                    "Supabase sign-up completed without a local session. Disable email confirmation for faster club onboarding.",
                )

            upsertClubProfile(
                context = context,
                userId = user.id,
                draft = sanitizedDraft,
            )
        }
    }

    suspend fun saveClubProfile(
        context: Context,
        draft: ClubProfileDraft,
    ): Result<Unit> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        val normalizedClubId = normalizeClubId(draft.clubId)
        val sanitizedDraft = draft.copy(
            clubId = normalizedClubId,
            contactEmail = draft.contactEmail.trim(),
            clubBannerColorHex = ClubBrandingUtils.normalizeBannerColorHex(draft.clubBannerColorHex),
        )
        val validationError = validateClubDraft(sanitizedDraft)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        return runCatching {
            val userId = SupabaseProvider.client(context).auth.currentSessionOrNull()?.user?.id
                ?: throw IllegalStateException("Sign in before saving your club profile.")

            upsertClubProfile(
                context = context,
                userId = userId,
                draft = sanitizedDraft,
            )
        }
    }

    suspend fun signOut(context: Context) {
        if (!backendConfigured) {
            return
        }

        SupabaseProvider.client(context).auth.signOut()
    }

    private suspend fun fetchOwnClubProfile(
        context: Context,
        userId: String,
    ): ClubProfile? = runCatching {
        SupabaseProvider.client(context)
            .from(clubProfilesTable)
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<ClubProfile>()
    }.getOrNull()

    private suspend fun fetchClubProfileByClubId(
        context: Context,
        clubId: String,
    ): ClubProfile? = runCatching {
        SupabaseProvider.client(context)
            .from(clubProfilesTable)
            .select {
                filter {
                    eq("club_id", clubId)
                }
            }
            .decodeSingleOrNull<ClubProfile>()
    }.getOrNull()

    private suspend fun upsertClubProfile(
        context: Context,
        userId: String,
        draft: ClubProfileDraft,
    ) {
        val client = SupabaseProvider.client(context)
        val payload = ClubProfile(
            userId = userId,
            clubId = draft.clubId,
            clubName = draft.clubName.trim(),
            clubLogoUrl = draft.clubLogoUrl.trim(),
            clubBannerColorHex = ClubBrandingUtils.normalizeBannerColorHex(draft.clubBannerColorHex),
            contactName = draft.contactName.trim(),
            contactEmail = draft.contactEmail.trim(),
            description = draft.description.trim(),
            updatedAtMillis = System.currentTimeMillis(),
        )

        client.from(clubProfilesTable)
            .upsert(
                payload,
                onConflict = "user_id",
            )

        client.from(eventsTable).update(
            {
                set("club_id", payload.clubId)
                set("club_name", payload.clubName)
                set("club_logo_url", payload.clubLogoUrl)
                set("club_banner_color_hex", payload.clubBannerColorHex)
                set("updated_at_millis", System.currentTimeMillis())
            },
        ) {
            filter {
                eq("club_owner_id", userId)
            }
        }
    }

    private fun validateClubDraft(
        draft: ClubProfileDraft,
        password: String? = null,
    ): String? {
        if (draft.clubId.isBlank()) {
            return "Add a club ID."
        }
        if (draft.clubName.isBlank()) {
            return "Add the club name."
        }
        if (draft.contactName.isBlank()) {
            return "Add the contact person's name."
        }
        if (draft.contactEmail.isBlank()) {
            return "Add a club email."
        }
        if (!draft.contactEmail.contains("@")) {
            return "Enter a valid club email."
        }
        if (draft.description.isBlank()) {
            return "Add a short club description."
        }
        if (password != null && password.length < 6) {
            return "Password should be at least 6 characters."
        }
        return null
    }

    private fun extractClubId(email: String): String = email.substringBefore("@").trim()

    private fun normalizeClubId(raw: String): String = raw
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')

    private companion object {
        const val clubProfilesTable = "club_profiles"
        const val eventsTable = "events"
    }
}
