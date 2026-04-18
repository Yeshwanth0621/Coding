package com.kkc.clubconnect.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kkc.clubconnect.data.AuthRepository
import com.kkc.clubconnect.data.ClubSession
import com.kkc.clubconnect.data.EventRepository
import com.kkc.clubconnect.data.MediaRepository
import com.kkc.clubconnect.model.ClubEvent
import com.kkc.clubconnect.model.ClubProfileDraft
import com.kkc.clubconnect.model.EventDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val backendConfigured: Boolean,
    val session: ClubSession = ClubSession(),
    val events: List<ClubEvent> = emptyList(),
    val isWorking: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class AdminViewModel(
    private val appContext: Context,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository,
    private val mediaRepository: MediaRepository,
    private val backendConfigured: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState(backendConfigured = backendConfigured))
    val uiState = _uiState.asStateFlow()
    private var cleanupInProgress = false
    private var lastCleanupTargetIds: Set<String> = emptySet()

    init {
        if (backendConfigured) {
            viewModelScope.launch {
                authRepository.observeSession(appContext)
                    .catch { error ->
                        _uiState.update {
                            it.copy(errorMessage = error.message ?: "Unable to verify club session.")
                        }
                    }
                    .collect { session ->
                        _uiState.update {
                            it.copy(session = session)
                        }
                        maybeAutoCleanupExpiredEvents()
                    }
            }

            viewModelScope.launch {
                eventRepository.observeEvents(appContext)
                    .catch { error ->
                        _uiState.update {
                            it.copy(errorMessage = error.message ?: "Unable to load club events.")
                        }
                    }
                    .collect { events ->
                        _uiState.update {
                            it.copy(events = events)
                        }
                        maybeAutoCleanupExpiredEvents()
                    }
            }
        }
    }

    fun signIn(clubId: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null, successMessage = null) }
            authRepository.signIn(
                context = appContext,
                clubId = clubId,
                password = password,
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            successMessage = "Club access unlocked.",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            errorMessage = error.message ?: "Club sign-in failed.",
                        )
                    }
                }
        }
    }

    fun registerClub(password: String, draft: ClubProfileDraft) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null, successMessage = null) }
            authRepository.registerClub(
                context = appContext,
                password = password,
                draft = draft,
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            successMessage = "Club access created. You can start posting events now.",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            errorMessage = error.message ?: "Unable to create the club access.",
                        )
                    }
                }
        }
    }

    fun saveClubProfile(draft: ClubProfileDraft) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null, successMessage = null) }
            authRepository.saveClubProfile(
                context = appContext,
                draft = draft,
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            successMessage = "Club profile updated.",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            errorMessage = error.message ?: "Unable to save the club profile.",
                        )
                    }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut(appContext)
            _uiState.update {
                it.copy(successMessage = "Signed out.")
            }
        }
    }

    fun saveEvent(eventId: String?, draft: EventDraft) {
        val session = _uiState.value.session
        if (!session.isSignedIn) {
            _uiState.update { it.copy(errorMessage = "Sign in before posting events.") }
            return
        }
        if (!session.hasCompletedSetup) {
            _uiState.update { it.copy(errorMessage = "Finish the club profile before posting events.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null, successMessage = null) }
            val existingEvent = _uiState.value.events.firstOrNull { it.id == eventId }

            val result = if (eventId.isNullOrBlank()) {
                eventRepository.createEvent(
                    context = appContext,
                    draft = draft,
                    session = session,
                )
            } else {
                eventRepository.updateEvent(
                    context = appContext,
                    eventId = eventId,
                    draft = draft,
                    session = session,
                )
            }

            result
                .onSuccess {
                    if (existingEvent != null && existingEvent.imageUrl != draft.imageUrl) {
                        mediaRepository.deleteManagedAssetFromUrl(appContext, existingEvent.imageUrl)
                    }
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            successMessage = if (eventId.isNullOrBlank()) {
                                "Event published."
                            } else {
                                "Event updated."
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            errorMessage = error.message ?: "Unable to save the event.",
                        )
                    }
                }
        }
    }

    fun deleteEvent(eventId: String) {
        val session = _uiState.value.session
        if (!session.isSignedIn || !session.hasCompletedSetup) {
            _uiState.update { it.copy(errorMessage = "Only signed-in clubs can remove their events.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null, successMessage = null) }
            val event = _uiState.value.events.firstOrNull { it.id == eventId }
            eventRepository.deleteEvent(
                context = appContext,
                eventId = eventId,
                session = session,
            )
                .onSuccess {
                    event?.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                        mediaRepository.deleteManagedAssetFromUrl(appContext, imageUrl)
                    }
                    _uiState.update {
                        it.copy(isWorking = false, successMessage = "Event deleted.")
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            errorMessage = error.message ?: "Unable to delete the event.",
                        )
                    }
                }
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    suspend fun uploadClubLogo(uri: Uri): Result<String> = mediaRepository.uploadClubLogo(
        context = appContext,
        imageUri = uri,
    )

    suspend fun uploadEventPoster(uri: Uri): Result<String> = mediaRepository.uploadEventPoster(
        context = appContext,
        imageUri = uri,
    )

    private fun maybeAutoCleanupExpiredEvents() {
        val state = _uiState.value
        val session = state.session
        if (!session.isSignedIn || !session.hasCompletedSetup || session.uid.isNullOrBlank()) {
            lastCleanupTargetIds = emptySet()
            return
        }

        val expiredOwnEvents = state.events.filter { event ->
            event.clubOwnerId == session.uid && event.endAtMillis < System.currentTimeMillis()
        }
        val expiredIds = expiredOwnEvents.map { it.id }.toSet()
        if (expiredIds.isEmpty()) {
            lastCleanupTargetIds = emptySet()
            return
        }
        if (cleanupInProgress || expiredIds == lastCleanupTargetIds) {
            return
        }

        cleanupInProgress = true
        lastCleanupTargetIds = expiredIds
        viewModelScope.launch {
            val cleanedCount = cleanupExpiredEvents(expiredOwnEvents, session)
            cleanupInProgress = false
            if (cleanedCount > 0) {
                _uiState.update {
                    it.copy(successMessage = "Cleaned $cleanedCount expired post(s) and reclaimed poster storage.")
                }
            } else {
                lastCleanupTargetIds = emptySet()
            }
        }
    }

    private suspend fun cleanupExpiredEvents(
        expiredEvents: List<ClubEvent>,
        session: ClubSession,
    ): Int {
        var cleanedCount = 0
        for (event in expiredEvents) {
            val deleteResult = eventRepository.deleteEvent(
                context = appContext,
                eventId = event.id,
                session = session,
            )
            if (deleteResult.isSuccess) {
                if (event.imageUrl.isNotBlank()) {
                    val imageCleanup = mediaRepository.deleteManagedAssetFromUrl(appContext, event.imageUrl)
                    if (imageCleanup.isFailure) {
                        _uiState.update {
                            it.copy(
                                errorMessage = imageCleanup.exceptionOrNull()?.message
                                    ?: "Expired event removed, but poster cleanup failed for one image.",
                            )
                        }
                    }
                }
                cleanedCount += 1
            } else {
                _uiState.update {
                    it.copy(errorMessage = deleteResult.exceptionOrNull()?.message ?: "Unable to remove an expired event.")
                }
            }
        }
        return cleanedCount
    }

    class Factory(
        private val appContext: Context,
        private val authRepository: AuthRepository,
        private val eventRepository: EventRepository,
        private val mediaRepository: MediaRepository,
        private val backendConfigured: Boolean,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminViewModel(
            appContext = appContext,
            authRepository = authRepository,
            eventRepository = eventRepository,
            mediaRepository = mediaRepository,
            backendConfigured = backendConfigured,
        ) as T
    }
}
