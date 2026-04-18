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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Sparkles
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kalamclub.booktopia.ui.components.FloatingOrb
import com.kalamclub.booktopia.ui.components.GlassCard
import com.kalamclub.booktopia.ui.components.GradientButton
import com.kalamclub.booktopia.ui.theme.*

/**
 * Landing/Welcome screen with hero section and feature cards.
 */
@Composable
fun LandingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToCommunity: () -> Unit
) {
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
                .align(Alignment.TopEnd)
                .padding(top = 120.dp, end = 20.dp),
            animationDelay = 1000
        )
        FloatingOrb(
            color = Pink500,
            size = 200.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 200.dp),
            animationDelay = 2000
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GradientIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoStories,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "Kalam Knowledge Club",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Inspiring minds, one page at a time",
                            style = MaterialTheme.typography.bodySmall,
                            color = Indigo300
                        )
                    }
                }
                
                TextButton(onClick = onNavigateToLogin) {
                    Text("Login", color = Indigo300)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Primary.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Sparkles,
                    contentDescription = null,
                    tint = Indigo300,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "  Booktopia Reading Challenge 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = Indigo300
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Hero title
            Text(
                text = buildAnnotatedString {
                    append("Track Your\n")
                    withStyle(SpanStyle(brush = GradientPrimary)) {
                        append("Reading Journey")
                    }
                },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Join the Booktopia challenge and compete with fellow readers. Log your daily pages, climb the leaderboard, and become the ultimate bookworm!",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // CTA Buttons
            GradientButton(
                text = "Start Reading Challenge",
                onClick = onNavigateToSignup,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "I have an account",
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Features section
            Text(
                text = "Why Join Booktopia?",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Make reading a daily habit and compete with your peers",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp)
            )
            
            // Feature cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    icon = Icons.Filled.CalendarMonth,
                    title = "Daily Tracking",
                    description = "Log your pages read every day",
                    gradient = GradientEmerald,
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    icon = Icons.Filled.EmojiEvents,
                    title = "Leaderboard",
                    description = "Compete with fellow readers",
                    gradient = GradientAmber,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    icon = Icons.Filled.Group,
                    title = "Community",
                    description = "Join the reading community",
                    gradient = GradientIndigo,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCommunity
                )
                FeatureCard(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "Track Progress",
                    description = "See your reading history",
                    gradient = GradientPurple,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Stats
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Join the Reading Revolution",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("500+", "Readers", GradientIndigo)
                        StatItem("50K+", "Pages", GradientAmber)
                        StatItem("30", "Day Challenge", GradientEmerald)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer
            HorizontalDivider(color = SurfaceCardBorder)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "\"You have to dream before your dreams can come true.\"",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "— A.P.J. Abdul Kalam",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    gradient: androidx.compose.ui.graphics.Brush,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GlassCard(modifier = modifier) {
        Column {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    gradient: androidx.compose.ui.graphics.Brush
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(brush = gradient),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
