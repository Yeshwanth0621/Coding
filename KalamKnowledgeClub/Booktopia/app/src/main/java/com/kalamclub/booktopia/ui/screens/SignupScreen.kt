package com.kalamclub.booktopia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.unit.sp
import com.kalamclub.booktopia.ui.components.BooktopiaTextField
import com.kalamclub.booktopia.ui.components.FloatingOrb
import com.kalamclub.booktopia.ui.components.GlassCard
import com.kalamclub.booktopia.ui.components.GradientButton
import com.kalamclub.booktopia.ui.theme.*
import com.kalamclub.booktopia.viewmodel.AuthViewModel

// Avatar options
private val defaultAvatars = listOf(
    "👨‍💻" to "boy1",
    "👨‍🎓" to "boy2",
    "🧑‍💼" to "boy3",
    "👨‍🔬" to "boy4",
    "🧑‍🚀" to "boy5",
    "👩‍💻" to "girl1",
    "👩‍🎓" to "girl2",
    "👩‍💼" to "girl3",
    "👩‍🔬" to "girl4",
    "👩‍🚀" to "girl5"
)

/**
 * Signup screen with email, password, username, and avatar selection.
 */
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf<String?>(null) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    
    // Validation
    val usernameError = when {
        username.isNotEmpty() && username.length < 3 -> "Username must be at least 3 characters"
        username.isNotEmpty() && !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Only letters, numbers, and underscores"
        else -> null
    }
    val passwordError = when {
        password.isNotEmpty() && password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }
    val confirmPasswordError = when {
        confirmPassword.isNotEmpty() && confirmPassword != password -> "Passwords do not match"
        else -> null
    }
    
    val isFormValid = username.length >= 3 &&
            email.isNotBlank() &&
            password.length >= 6 &&
            password == confirmPassword &&
            usernameError == null
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground)
    ) {
        // Background orbs
        FloatingOrb(
            color = Secondary,
            size = 280.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 50.dp)
        )
        FloatingOrb(
            color = Pink500,
            size = 320.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Signup card
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
                            .background(GradientPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Join Booktopia",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Start your reading challenge today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Google Sign Up
                    GradientButton(
                        text = "Sign up with Google",
                        onClick = { /* Google sign up would go here */ },
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
                    
                    // Avatar picker
                    Text(
                        text = "Profile Picture (optional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(2.dp, if (selectedAvatar != null) Primary else SurfaceCardBorder, CircleShape)
                            .clickable { showAvatarPicker = !showAvatarPicker },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAvatar != null) {
                            Text(
                                text = selectedAvatar!!,
                                fontSize = 36.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Choose avatar",
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    
                    // Avatar grid
                    if (showAvatarPicker) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(120.dp)
                        ) {
                            items(defaultAvatars) { (emoji, _) ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedAvatar == emoji) Primary.copy(alpha = 0.3f) else SurfaceCard)
                                        .border(
                                            2.dp,
                                            if (selectedAvatar == emoji) Primary else SurfaceCardBorder,
                                            CircleShape
                                        )
                                        .clickable {
                                            selectedAvatar = emoji
                                            showAvatarPicker = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 24.sp)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Username field
                    BooktopiaTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username *",
                        leadingIcon = Icons.Filled.Person,
                        isError = usernameError != null,
                        errorMessage = usernameError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email field
                    BooktopiaTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address *",
                        leadingIcon = Icons.Filled.Email,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password field
                    BooktopiaTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password *",
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Confirm password field
                    BooktopiaTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password *",
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        isError = confirmPasswordError != null,
                        errorMessage = confirmPasswordError,
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
                    
                    // Create account button
                    GradientButton(
                        text = "Create Account",
                        onClick = {
                            authViewModel.signUp(
                                email = email,
                                password = password,
                                username = username,
                                avatarUrl = selectedAvatar,
                                onSuccess = onNavigateToLogin
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        gradient = GradientPurple,
                        isLoading = isLoading,
                        enabled = isFormValid
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Login link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Sign in", color = Secondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "By signing up, you agree to participate in the Booktopia reading challenge.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
