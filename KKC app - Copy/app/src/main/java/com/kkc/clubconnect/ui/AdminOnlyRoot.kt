package com.kkc.clubconnect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkc.clubconnect.AppContainer
import com.kkc.clubconnect.ui.screens.AdminScreen
import com.kkc.clubconnect.ui.theme.Paper
import com.kkc.clubconnect.viewmodel.AdminViewModel

@Composable
fun AdminOnlyRoot(appContainer: AppContainer) {
    val appContext = LocalContext.current.applicationContext
    val viewModel: AdminViewModel = viewModel(
        factory = AdminViewModel.Factory(
            appContext = appContext,
            authRepository = appContainer.authRepository,
            eventRepository = appContainer.eventRepository,
            mediaRepository = appContainer.mediaRepository,
            backendConfigured = appContainer.backendConfigured,
        ),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = Paper) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent),
        ) {
            AdminScreen(
                uiState = uiState.value,
                onSignIn = viewModel::signIn,
                onRegisterClub = viewModel::registerClub,
                onSaveClubProfile = viewModel::saveClubProfile,
                onUploadClubLogo = viewModel::uploadClubLogo,
                onUploadEventPoster = viewModel::uploadEventPoster,
                onSignOut = viewModel::signOut,
                onSaveEvent = viewModel::saveEvent,
                onDeleteEvent = viewModel::deleteEvent,
                onClearBanner = viewModel::clearBanner,
            )
        }
    }
}
