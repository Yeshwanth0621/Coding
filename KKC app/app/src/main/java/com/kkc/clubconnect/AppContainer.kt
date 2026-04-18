package com.kkc.clubconnect

import com.kkc.clubconnect.data.AuthRepository
import com.kkc.clubconnect.data.EventRepository
import com.kkc.clubconnect.data.MediaRepository

class AppContainer(
    val backendConfigured: Boolean,
    val eventRepository: EventRepository = EventRepository(backendConfigured),
    val authRepository: AuthRepository = AuthRepository(backendConfigured),
    val mediaRepository: MediaRepository = MediaRepository(backendConfigured),
)
