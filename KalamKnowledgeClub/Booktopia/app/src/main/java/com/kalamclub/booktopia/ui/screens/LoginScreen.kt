package com.kalamclub.booktopia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kalamclub.booktopia.ui.components.BooktopiaTextField
import com.kalamclub.booktopia.ui.components.FloatingOrb
import com.kalamclub.booktopia.ui.components.GlassCard
import com.kalamclub.booktopia.ui.components.GradientButton
import com.kalamclub.booktopia.ui.theme.*
import com.kalamclub.booktopia.viewmodel.AuthViewModel

/**
 * Login screen with Google and email/password authentication.
 */
@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground)
    ) {
        // Background orbs
        FloatingOrb(
            color = Primary,
            size = 280.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 50.dp)
        )
        FloatingOrb(
            color = Secondary,
            size = 320.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp),
            animationDelay = 1000
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GradientIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoStories,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Login to track your reading progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Google Sign In - simplified (actual implementation needs Credential Manager)
                    GradientButton(
                        text = "Sign in with Google",
                        onClick = { /* Google sign in would go here */ },
                        modifier = Modifier.fillMaxWidth(),
                        gradient = GradientBackground
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceCardBorder)
                        Text("or continue with email", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceCardBorder)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Email field
                    BooktopiaTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        leadingIcon = Icons.Filled.Email,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password field
                    BooktopiaTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Error message
                    if (error != null) {
                        Text(
                            text = error!!,
                            color = Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Sign in button
                    GradientButton(
                        text = "Sign In",
                        onClick = { authViewModel.signIn(email, password) },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isLoading,
                        enabled = email.isNotBlank() && password.isNotBlank()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sign up link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        TextButton(onClick = onNavigateToSignup) {
                            Text("Sign up", color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer quote
            Text(
                text = "\"You have to dream before your dreams can come true.\"\n— A.P.J. Abdul Kalam",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
