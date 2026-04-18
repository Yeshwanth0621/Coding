package com.kkc.clubconnect.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kkc.clubconnect.data.EventRepository
import com.kkc.clubconnect.model.ClubEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HomeUiState(
    val backendConfigured: Boolean,
    val isLoading: Boolean = true,
    val featuredEvent: ClubEvent? = null,
    val events: List<ClubEvent> = emptyList(),
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val appContext: Context,
    private val eventRepository: EventRepository,
    private val backendConfigured: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(backendConfigured = backendConfigured, isLoading = backendConfigured),
    )
    val uiState = _uiState.asStateFlow()

    init {
        if (backendConfigured) {
            viewModelScope.launch {
                eventRepository.observeEvents(appContext)
                    .catch { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Unable to load events right now.",
                            )
                        }
                    }
                    .collect { events ->
                        applyVisibleEvents(events)
                    }
            }

            viewModelScope.launch {
                while (isActive) {
                    eventRepository.fetchUpcomingEvents(appContext)
                        .onSuccess { events ->
                            applyVisibleEvents(events)
                        }
                        .onFailure { error ->
                            _uiState.update { state ->
                                if (state.events.isNotEmpty()) {
                                    state.copy(isLoading = false)
                                } else {
                                    state.copy(
                                        isLoading = false,
                                        errorMessage = error.message ?: "Unable to refresh events right now.",
                                    )
                                }
                            }
                        }
                    delay(foregroundRefreshIntervalMillis)
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun applyVisibleEvents(events: List<ClubEvent>) {
        val now = System.currentTimeMillis()
        val visibleEvents = events
            .filter { it.endAtMillis >= now }
            .sortedWith(
                compareByDescending<ClubEvent> { it.featured }
                    .thenBy { it.startAtMillis },
            )
        _uiState.update {
            it.copy(
                isLoading = false,
                featuredEvent = visibleEvents.firstOrNull { event -> event.featured }
                    ?: visibleEvents.firstOrNull(),
                events = visibleEvents,
                errorMessage = null,
            )
        }
    }

    private companion object {
        const val foregroundRefreshIntervalMillis = 15_000L
    }

    class Factory(
        private val appContext: Context,
        private val eventRepository: EventRepository,
        private val backendConfigured: Boolean,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(
            appContext = appContext,
            eventRepository = eventRepository,
            backendConfigured = backendConfigured,
        ) as T
    }
}
