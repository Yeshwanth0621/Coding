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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalamclub.booktopia.ui.components.FloatingOrb
import com.kalamclub.booktopia.ui.components.GlassCard
import com.kalamclub.booktopia.ui.components.GradientButton
import com.kalamclub.booktopia.ui.theme.*

// Sample featured members data
private val featuredMembers = listOf(
    Triple("Rahul Sharma", 2450, "👨‍💻"),
    Triple("Priya Patel", 2100, "👩‍🎓"),
    Triple("Amit Kumar", 1890, "📚"),
    Triple("Sneha Reddy", 1750, "🎯"),
    Triple("Vikram Singh", 1620, "🌟"),
    Triple("Ananya Das", 1580, "💡")
)

/**
 * Community screen showing stats, featured members, and values.
 */
@Composable
fun CommunityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground)
    ) {
        // Background orbs
        FloatingOrb(
            color = Emerald500,
            size = 300.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 50.dp)
        )
        FloatingOrb(
            color = Teal500,
            size = 250.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp),
            animationDelay = 1000
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            item {
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GradientEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Group, null, tint = TextPrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Community",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Kalam Knowledge Club",
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald400
                            )
                        }
                    }
                    
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextSecondary)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Hero
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(GradientEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Group, null, tint = TextPrimary, modifier = Modifier.size(48.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Our Reading Community",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        "Join hundreds of passionate readers from Kalam Knowledge Club. Together, we're building a culture of reading and continuous learning.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )
                }
            }
            
            // Stats grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox("500+", "Active Readers", Modifier.weight(1f))
                    StatBox("50K+", "Pages Read", Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox("30", "Day Challenge", Modifier.weight(1f))
                    StatBox("15+", "Departments", Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Featured readers
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.Star, null, tint = Amber500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Featured Readers",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            items(featuredMembers) { (name, pages, avatar) ->
                MemberCard(name = name, pages = pages, avatar = avatar)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Values section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Favorite, null, tint = Primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Our Values",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ValueItem(
                            icon = Icons.Filled.AutoStories,
                            title = "Continuous Learning",
                            description = "We believe in the power of daily reading to transform minds and build knowledge."
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ValueItem(
                            icon = Icons.Filled.Group,
                            title = "Support Each Other",
                            description = "Our community celebrates every achievement, big or small. We grow together."
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ValueItem(
                            icon = Icons.Filled.Star,
                            title = "Inspire Excellence",
                            description = "Following APJ Abdul Kalam's vision of dreaming big and achieving more."
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // CTA
            item {
                GradientButton(
                    text = "Join Our Community",
                    onClick = onNavigateToSignup,
                    gradient = GradientEmerald,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Footer
            item {
                HorizontalDivider(color = SurfaceCardBorder)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "\"You have to dream before your dreams can come true.\"\n— A.P.J. Abdul Kalam",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
            }
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun MemberCard(name: String, pages: Int, avatar: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Emerald500.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(avatar, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("$pages pages read", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Icon(Icons.Filled.Favorite, null, tint = Emerald400, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ValueItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(12.dp)
    ) {
        Icon(
            icon,
            null,
            tint = Primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
