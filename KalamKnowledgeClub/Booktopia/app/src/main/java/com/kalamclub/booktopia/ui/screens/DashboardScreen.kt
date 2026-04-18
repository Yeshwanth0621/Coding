package com.kalamclub.booktopia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalamclub.booktopia.data.DailyReading
import com.kalamclub.booktopia.data.LeaderboardEntry
import com.kalamclub.booktopia.ui.components.FloatingOrb
import com.kalamclub.booktopia.ui.components.GlassCard
import com.kalamclub.booktopia.ui.components.GradientButton
import com.kalamclub.booktopia.ui.components.LoadingSpinner
import com.kalamclub.booktopia.ui.components.SectionHeader
import com.kalamclub.booktopia.ui.components.StatsCard
import com.kalamclub.booktopia.ui.theme.*
import com.kalamclub.booktopia.viewmodel.AuthViewModel
import com.kalamclub.booktopia.viewmodel.DashboardViewModel

/**
 * Main dashboard screen with tabs for Log Pages, Leaderboard, and History.
 */
@Composable
fun DashboardScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val isLoading by dashboardViewModel.isLoading.collectAsState()
    val todayReading by dashboardViewModel.todayReading.collectAsState()
    val totalPages by dashboardViewModel.totalPages.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Load data when user is available
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            dashboardViewModel.loadDashboardData(user.id)
        }
    }
    
    val displayName = userProfile?.username ?: currentUser?.email?.substringBefore("@") ?: "Reader"
    
    Scaffold(
        containerColor = BgDarkMid,
        bottomBar = {
            DashboardBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GradientBackground)
                .padding(paddingValues)
        ) {
            // Background orbs
            FloatingOrb(
                color = Primary,
                size = 300.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 0.dp)
            )
            FloatingOrb(
                color = Secondary,
                size = 250.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp),
                animationDelay = 1000
            )
            
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                DashboardHeader(
                    email = currentUser?.email ?: "",
                    onHomeClick = onNavigateToHome,
                    onSignOut = onSignOut
                )
                
                // Welcome section
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "${dashboardViewModel.getGreeting()}, $displayName! 📚",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ready to log your reading progress today?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                // Stats cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = "Total Pages",
                        value = totalPages.toString(),
                        icon = Icons.Filled.BookmarkAdded,
                        gradient = GradientIndigo,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Pages Today",
                        value = (todayReading?.pagesRead ?: 0).toString(),
                        icon = Icons.Filled.CalendarMonth,
                        gradient = GradientAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Today's status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    StatsCard(
                        title = "Today's Status",
                        value = if (todayReading != null) "✅ Done" else "⏳ Pending",
                        icon = Icons.Filled.TrendingUp,
                        gradient = GradientEmerald,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tab content
                GlassCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingSpinner()
                        }
                    } else {
                        when (selectedTab) {
                            0 -> LogPagesTab(
                                userId = currentUser?.id ?: "",
                                email = currentUser?.email ?: "",
                                dashboardViewModel = dashboardViewModel
                            )
                            1 -> LeaderboardTab(
                                currentUserEmail = currentUser?.email ?: "",
                                dashboardViewModel = dashboardViewModel
                            )
                            2 -> HistoryTab(dashboardViewModel = dashboardViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    email: String,
    onHomeClick: () -> Unit,
    onSignOut: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(top = 32.dp),
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
                    text = "Booktopia",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kalam Knowledge Club",
                    style = MaterialTheme.typography.bodySmall,
                    color = Indigo300
                )
            }
        }
        
        Row {
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = TextSecondary
                )
            }
            IconButton(onClick = onSignOut) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign out",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DashboardBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = BgDarkStart.copy(alpha = 0.95f),
        contentColor = TextPrimary
    ) {
        val items = listOf(
            Triple(Icons.Filled.AutoStories, "Log Pages", 0),
            Triple(Icons.Filled.EmojiEvents, "Leaderboard", 1),
            Triple(Icons.Filled.History, "History", 2)
        )
        
        items.forEach { (icon, label, index) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TextPrimary,
                    selectedTextColor = TextPrimary,
                    indicatorColor = Primary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

// ==================== LOG PAGES TAB ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogPagesTab(
    userId: String,
    email: String,
    dashboardViewModel: DashboardViewModel
) {
    val selectedDate by dashboardViewModel.selectedDate.collectAsState()
    val selectedDateReading by dashboardViewModel.selectedDateReading.collectAsState()
    val submitSuccess by dashboardViewModel.submitSuccess.collectAsState()
    val isLoading by dashboardViewModel.isLoading.collectAsState()
    
    var pagesInput by remember { mutableStateOf("") }
    var dateDropdownExpanded by remember { mutableStateOf(false) }
    
    // Update input when date changes
    LaunchedEffect(selectedDateReading) {
        pagesInput = selectedDateReading?.pagesRead?.toString() ?: ""
    }
    
    // Handle submit success
    LaunchedEffect(submitSuccess) {
        if (submitSuccess == true) {
            dashboardViewModel.clearSubmitSuccess()
        }
    }
    
    val quickSelectValues = listOf(5, 10, 15, 20, 25, 30, 50)
    val selectedDateLabel = dashboardViewModel.allowedDates.find { it.value == selectedDate }?.label ?: "Today"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(
            title = if (selectedDateReading != null) "Update Reading" else "Log Reading",
            subtitle = selectedDate,
            icon = Icons.Filled.AutoStories,
            iconGradient = GradientIndigo
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Date selector
        Text(
            text = "Select Date",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .clickable { dateDropdownExpanded = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = Primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = selectedDateLabel, color = TextPrimary)
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            
            DropdownMenu(
                expanded = dateDropdownExpanded,
                onDismissRequest = { dateDropdownExpanded = false },
                modifier = Modifier.background(Slate800)
            ) {
                dashboardViewModel.allowedDates.forEach { dateOption ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(dateOption.label, color = TextPrimary)
                                if (selectedDate == dateOption.value) {
                                    Icon(Icons.Filled.Check, null, tint = Primary)
                                }
                            }
                        },
                        onClick = {
                            dashboardViewModel.selectDate(dateOption.value, userId)
                            dateDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        // Already logged banner
        if (selectedDateReading != null) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Success.copy(alpha = 0.2f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Check, null, tint = Success)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Already logged for ${selectedDateLabel.lowercase()}! ✨",
                        color = Success,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "You read ${selectedDateReading!!.pagesRead} pages. You can update below.",
                        color = Success.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Pages input
        Text(
            text = "How many pages did you read?",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = pagesInput,
            onValueChange = { pagesInput = it.filter { c -> c.isDigit() } },
            placeholder = { Text("Enter pages", color = TextMuted) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                textAlign = TextAlign.Center,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = SurfaceCardBorder,
                cursorColor = Primary,
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Text(
            text = "Maximum 1000 pages per day",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick select
        Text(
            text = "Quick select:",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickSelectValues) { value ->
                val isSelected = pagesInput == value.toString()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Primary else SurfaceCard)
                        .clickable { pagesInput = value.toString() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = value.toString(),
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Submit button
        GradientButton(
            text = if (selectedDateReading != null) "Update Entry" else "Log Reading",
            onClick = {
                val pages = pagesInput.toIntOrNull()
                if (pages != null && pages in 1..1000) {
                    dashboardViewModel.submitReading(userId, email, pages)
                }
            },
            isLoading = isLoading,
            enabled = pagesInput.toIntOrNull()?.let { it in 1..1000 } == true,
            icon = if (selectedDateReading != null) Icons.Filled.Refresh else Icons.Filled.Check,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Warning.copy(alpha = 0.1f))
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Filled.Info, null, tint = Warning, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Flexible logging", color = Warning, fontWeight = FontWeight.Medium)
                Text(
                    "Forgot to log yesterday? You can submit for today and up to 2 days in the past.",
                    color = Warning.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ==================== LEADERBOARD TAB ====================

@Composable
private fun LeaderboardTab(
    currentUserEmail: String,
    dashboardViewModel: DashboardViewModel
) {
    val leaderboard by dashboardViewModel.leaderboard.collectAsState()
    val userRank = dashboardViewModel.getUserRank(currentUserEmail)
    
    Column(modifier = Modifier.fillMaxSize()) {
        SectionHeader(
            title = "Leaderboard",
            subtitle = "Top readers of Booktopia",
            icon = Icons.Filled.EmojiEvents,
            iconGradient = GradientAmber
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User rank card
        if (userRank != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.2f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = Indigo300)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Your current rank", color = Indigo300)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "#$userRank",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        " of ${leaderboard.size}",
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Leaderboard list
        if (leaderboard.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No entries yet", color = TextSecondary)
                    Text("Be the first to log your reading!", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(leaderboard) { index, entry ->
                    LeaderboardItem(
                        entry = entry,
                        rank = index + 1,
                        isCurrentUser = entry.email == currentUserEmail
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Refresh button
        TextButton(
            onClick = { dashboardViewModel.loadLeaderboard() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Refresh, null, tint = TextSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh Leaderboard", color = TextSecondary)
        }
    }
}

@Composable
private fun LeaderboardItem(
    entry: LeaderboardEntry,
    rank: Int,
    isCurrentUser: Boolean
) {
    val backgroundColor = when (rank) {
        1 -> Gold.copy(alpha = 0.2f)
        2 -> Silver.copy(alpha = 0.2f)
        3 -> Bronze.copy(alpha = 0.2f)
        else -> SurfaceCard
    }
    
    val rankIcon: @Composable () -> Unit = {
        when (rank) {
            1 -> Text("👑", style = MaterialTheme.typography.titleLarge)
            2 -> Text("🥈", style = MaterialTheme.typography.titleLarge)
            3 -> Text("🥉", style = MaterialTheme.typography.titleLarge)
            else -> Text(
                rank.toString(),
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (isCurrentUser) Modifier.background(Primary.copy(alpha = 0.1f))
                else Modifier
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                rankIcon()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        color = if (isCurrentUser) Indigo300 else TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isCurrentUser) {
                        Text(" (You)", color = Indigo300, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text(
                    entry.email,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                entry.totalPages.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text("pages", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ==================== HISTORY TAB ====================

@Composable
private fun HistoryTab(
    dashboardViewModel: DashboardViewModel
) {
    val readingHistory by dashboardViewModel.readingHistory.collectAsState()
    val totalPages by dashboardViewModel.totalPages.collectAsState()
    
    val daysLogged = readingHistory.size
    val avgPages = if (daysLogged > 0) totalPages / daysLogged else 0
    val todayDate = dashboardViewModel.allowedDates.first().value
    
    Column(modifier = Modifier.fillMaxSize()) {
        SectionHeader(
            title = "Reading History",
            subtitle = "Your daily reading log",
            icon = Icons.Filled.History,
            iconGradient = GradientEmerald
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        daysLogged.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Days Logged", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        avgPages.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Avg Pages/Day", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // History list
        if (readingHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AutoStories, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No reading history yet", color = TextSecondary)
                    Text("Start by logging your first reading!", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(readingHistory) { reading ->
                    HistoryItem(reading = reading, isToday = reading.readingDate == todayDate)
                }
            }
        }
        
        // Total summary
        if (readingHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GradientIndigo)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Pages Read", color = TextSecondary, fontWeight = FontWeight.Medium)
                Text(
                    totalPages.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(reading: DailyReading, isToday: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isToday) Primary.copy(alpha = 0.2f) else SurfaceCard)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isToday) Primary.copy(alpha = 0.3f) else SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoStories,
                    null,
                    tint = if (isToday) Indigo300 else TextMuted
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(reading.readingDate, color = TextPrimary, fontWeight = FontWeight.Medium)
                    if (isToday) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Primary.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Today", color = Indigo300, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (reading.updatedAt != reading.createdAt) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Edit, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Updated", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                reading.pagesRead.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text("pages", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
