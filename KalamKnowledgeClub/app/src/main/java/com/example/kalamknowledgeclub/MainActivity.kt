package com.example.kalamknowledgeclub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kalamknowledgeclub.ui.theme.KalamKnowledgeClubTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// --- Data Models ---
data class DailyEntry(
    val date: String,
    val pages: Int,
    val uploadTime: String
)

data class Member(
    val id: String,
    val name: String,
    val totalPages: Int,
    val dailyProgress: List<DailyEntry> = emptyList(),
    val streak: Int = 0,
    val dailyGoal: Int = 50
)

// --- ViewModel ---
class BooktopiaViewModel : ViewModel() {
    private val _members = MutableStateFlow(
        listOf(
            Member("1", "You", 125, listOf(DailyEntry(LocalDate.now().minusDays(1).toString(), 125, "22:30")), 1),
            Member("2", "Aaryan", 450, listOf(DailyEntry(LocalDate.now().minusDays(1).toString(), 150, "23:00")), 5),
            Member("3", "Sneha", 380, listOf(DailyEntry(LocalDate.now().minusDays(1).toString(), 80, "22:15")), 3),
            Member("4", "Rahul", 310, listOf(DailyEntry(LocalDate.now().minusDays(1).toString(), 110, "23:15")), 2),
            Member("5", "Priya", 290, listOf(DailyEntry(LocalDate.now().minusDays(1).toString(), 90, "22:45")), 4)
        )
    )
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _currentUser = MutableStateFlow(_members.value.first { it.id == "1" })
    val currentUser: StateFlow<Member> = _currentUser.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val quotes = listOf(
        "A book is a dream that you hold in your hand.",
        "Reading is to the mind what exercise is to the body.",
        "Today a reader, tomorrow a leader.",
        "Books are a uniquely portable magic."
    )
    val currentQuote = quotes.random()

    fun uploadProgress(pages: Int) {
        viewModelScope.launch {
            _isUploading.value = true
            delay(1500) // Realistic sync delay
            
            val today = LocalDate.now().toString()
            val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            
            val currentEntries = _currentUser.value.dailyProgress.toMutableList()
            val existingTodayIndex = currentEntries.indexOfFirst { it.date == today }
            
            if (existingTodayIndex != -1) {
                currentEntries[existingTodayIndex] = DailyEntry(today, pages, nowTime)
            } else {
                currentEntries.add(0, DailyEntry(today, pages, nowTime))
            }
            
            val newTotal = currentEntries.sumOf { it.pages }
            val updatedUser = _currentUser.value.copy(
                totalPages = newTotal,
                dailyProgress = currentEntries,
                streak = if (pages > 0) _currentUser.value.streak + 1 else _currentUser.value.streak
            )
            
            _currentUser.value = updatedUser
            _members.value = _members.value.map {
                if (it.id == "1") updatedUser else it
            }.sortedByDescending { it.totalPages }
            
            _isUploading.value = false
        }
    }
}

// --- Main Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KalamKnowledgeClubTheme {
                BooktopiaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooktopiaApp(viewModel: BooktopiaViewModel = viewModel()) {
    val members by viewModel.members.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Kalam Knowledge Club",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "BookTopia",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isUploading) {
                ExtendedFloatingActionButton(
                    onClick = { showUploadDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Nightly Sync", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                AchievementCard(currentUser, isUploading)
            }
            
            item {
                QuoteCard(viewModel.currentQuote)
            }
            
            item {
                Text(
                    "Club Leaderboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            
            items(members) { member ->
                LeaderboardItem(
                    member = member,
                    isMe = member.id == currentUser.id,
                    onClick = { selectedMember = member }
                )
            }
        }

        if (showUploadDialog) {
            NightlyUploadDialog(
                onDismiss = { showUploadDialog = false },
                onConfirm = { pages ->
                    viewModel.uploadProgress(pages)
                    showUploadDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Uploading $pages pages...")
                    }
                }
            )
        }

        if (selectedMember != null) {
            MemberHistorySheet(
                member = selectedMember!!,
                onDismiss = { selectedMember = null }
            )
        }
    }
}

@Composable
fun AchievementCard(user: Member, isUploading: Boolean) {
    val todayDate = LocalDate.now().toString()
    val todayEntry = user.dailyProgress.find { it.date == todayDate }
    val progress = (todayEntry?.pages?.toFloat() ?: 0f) / user.dailyGoal.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                    )
                )
                .padding(28.dp)
        ) {
            if (isUploading) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text("Syncing Journey...", color = Color.White, fontWeight = FontWeight.Medium)
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Achievement", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                            Text("${user.totalPages} Pages", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                        }
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { progress.coerceAtMost(1f) },
                                modifier = Modifier.size(60.dp),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f),
                                strokeWidth = 6.dp
                            )
                            Icon(Icons.Default.Star, contentDescription = "Goal Status", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (todayEntry != null) "Today: ${todayEntry.pages}/${user.dailyGoal} pages synced."
                                else "Ready to sync today's reading?",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteCard(quote: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = "Quote", tint = MaterialTheme.colorScheme.primary)
            Text(
                quote,
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun LeaderboardItem(member: Member, isMe: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = if (isMe) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text(member.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(if (isMe) "You" else member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Streak", modifier = Modifier.size(14.dp), tint = Color(0xFFFFA000))
                    Text("${member.streak} day streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("${member.totalPages}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("total pages", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberHistorySheet(member: Member, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)
        ) {
            Text("${member.name}'s Activity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(24.dp))
            
            if (member.dailyProgress.isEmpty()) {
                Text("No data synced yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                member.dailyProgress.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(formatDate(entry.date), fontWeight = FontWeight.Bold)
                            Text("Synced @ ${entry.uploadTime}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("+${entry.pages}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun NightlyUploadDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var pagesText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary) },
        title = { Text("BookTopia Nightly Sync", textAlign = TextAlign.Center) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Submit your day's progress to the club. How many pages did you finish today?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                OutlinedTextField(
                    value = pagesText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) pagesText = it },
                    label = { Text("Pages Finished Today") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val p = pagesText.toIntOrNull() ?: 0
                    onConfirm(p)
                },
                enabled = pagesText.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sync Progress", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}

fun formatDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val today = LocalDate.now()
        when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM dd"))
        }
    } catch (_: Exception) {
        dateStr
    }
}
