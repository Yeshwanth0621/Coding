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
import com.kkc.clubconnect.ui.screens.HomeScreen
import com.kkc.clubconnect.ui.theme.Paper
import com.kkc.clubconnect.viewmodel.HomeViewModel

@Composable
fun ClubConnectRoot(appContainer: AppContainer) {
    val appContext = LocalContext.current.applicationContext
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            appContext = appContext,
            eventRepository = appContainer.eventRepository,
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
            HomeScreen(uiState = uiState.value)
        }
    }
}
