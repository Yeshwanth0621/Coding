package com.kalamclub.booktopia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalamclub.booktopia.data.BooktopiaRepository
import com.kalamclub.booktopia.data.DailyReading
import com.kalamclub.booktopia.data.DateOption
import com.kalamclub.booktopia.data.LeaderboardEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for dashboard data management.
 */
class DashboardViewModel : ViewModel() {
    
    // ==================== STATE ====================
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _todayReading = MutableStateFlow<DailyReading?>(null)
    val todayReading: StateFlow<DailyReading?> = _todayReading.asStateFlow()
    
    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()
    
    private val _readingHistory = MutableStateFlow<List<DailyReading>>(emptyList())
    val readingHistory: StateFlow<List<DailyReading>> = _readingHistory.asStateFlow()
    
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(BooktopiaRepository.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()
    
    private val _selectedDateReading = MutableStateFlow<DailyReading?>(null)
    val selectedDateReading: StateFlow<DailyReading?> = _selectedDateReading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _submitSuccess = MutableStateFlow<Boolean?>(null)
    val submitSuccess: StateFlow<Boolean?> = _submitSuccess.asStateFlow()
    
    val allowedDates: List<DateOption> = BooktopiaRepository.getAllowedDates()
    
    // ==================== DATA LOADING ====================
    
    /**
     * Load all dashboard data for a user.
     */
    fun loadDashboardData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Load today's reading
                BooktopiaRepository.getTodayReading(userId).onSuccess { reading ->
                    _todayReading.value = reading
                }
                
                // Load total pages
                _totalPages.value = BooktopiaRepository.getUserTotalPages(userId)
                
                // Load reading history
                BooktopiaRepository.getUserReadings(userId).onSuccess { readings ->
                    _readingHistory.value = readings
                }
                
                // Load leaderboard
                loadLeaderboard()
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load leaderboard data.
     */
    fun loadLeaderboard() {
        viewModelScope.launch {
            BooktopiaRepository.getLeaderboard().onSuccess { entries ->
                _leaderboard.value = entries
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }
    
    // ==================== DATE SELECTION ====================
    
    /**
     * Select a date for logging.
     */
    fun selectDate(date: String, userId: String) {
        viewModelScope.launch {
            _selectedDate.value = date
            
            // Load reading for selected date
            BooktopiaRepository.getReadingForDate(userId, date).onSuccess { reading ->
                _selectedDateReading.value = reading
            }
        }
    }
    
    // ==================== SUBMIT READING ====================
    
    /**
     * Submit or update a reading entry.
     */
    fun submitReading(
        userId: String,
        email: String,
        pagesRead: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _submitSuccess.value = null
            
            val date = _selectedDate.value
            
            BooktopiaRepository.submitReading(userId, email, pagesRead, date)
                .onSuccess { isUpdate ->
                    _submitSuccess.value = true
                    // Refresh data
                    loadDashboardData(userId)
                }
                .onFailure { e ->
                    _error.value = e.message
                    _submitSuccess.value = false
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Get user's rank in leaderboard.
     */
    fun getUserRank(email: String): Int? {
        return _leaderboard.value.find { it.email == email }?.rank
    }
    
    /**
     * Clear submit success state.
     */
    fun clearSubmitSuccess() {
        _submitSuccess.value = null
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Get the greeting based on time of day.
     */
    fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}
