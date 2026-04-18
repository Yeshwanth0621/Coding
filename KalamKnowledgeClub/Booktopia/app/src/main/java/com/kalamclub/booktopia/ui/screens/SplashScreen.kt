package com.kalamclub.booktopia.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalamclub.booktopia.ui.components.FloatingOrb
import com.kalamclub.booktopia.ui.theme.*
import com.kalamclub.booktopia.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

/**
 * Splash screen with animated logo and auth state check.
 */
@Composable
fun SplashScreen(
    onNavigateToLanding: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    authViewModel: AuthViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    
    var animationStarted by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )
    
    LaunchedEffect(Unit) {
        animationStarted = true
    }
    
    // Navigate after animation and auth check
    LaunchedEffect(isLoading) {
        delay(2000) // Wait for animation
        if (!isLoading) {
            if (isLoggedIn) {
                onNavigateToDashboard()
            } else {
                onNavigateToLanding()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground),
        contentAlignment = Alignment.Center
    ) {
        // Background orbs
        FloatingOrb(
            color = Primary,
            size = 200.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 100.dp, start = 20.dp)
        )
        FloatingOrb(
            color = Secondary,
            size = 250.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp),
            animationDelay = 1000
        )
        
        // Logo and title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GradientIndigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoStories,
                    contentDescription = "Booktopia",
                    tint = TextPrimary,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Text(
                text = "Booktopia",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )
            
            Text(
                text = "Kalam Knowledge Club",
                style = MaterialTheme.typography.bodyLarge,
                color = Indigo300,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
