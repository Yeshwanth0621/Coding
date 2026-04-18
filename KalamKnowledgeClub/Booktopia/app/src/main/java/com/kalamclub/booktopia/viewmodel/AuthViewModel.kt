package com.kalamclub.booktopia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalamclub.booktopia.data.BooktopiaRepository
import com.kalamclub.booktopia.data.UserProfile
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication state management.
 */
class AuthViewModel : ViewModel() {
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    /**
     * Check current authentication status.
     */
    fun checkAuthStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = BooktopiaRepository.getCurrentUser()
                _currentUser.value = user
                _isLoggedIn.value = user != null
                
                if (user != null) {
                    loadUserProfile(user.id)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load user profile from database.
     */
    private suspend fun loadUserProfile(userId: String) {
        BooktopiaRepository.getUserProfile(userId).onSuccess { profile ->
            _userProfile.value = profile
        }
    }
    
    /**
     * Sign in with email and password.
     */
    fun signIn(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            BooktopiaRepository.signIn(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    loadUserProfile(user.id)
                    onSuccess()
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Sign in failed"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Sign up with email, password, and username.
     */
    fun signUp(
        email: String, 
        password: String, 
        username: String,
        avatarUrl: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Check username availability first
            if (!BooktopiaRepository.isUsernameAvailable(username)) {
                _error.value = "Username is already taken"
                _isLoading.value = false
                return@launch
            }
            
            BooktopiaRepository.signUp(email, password, username, avatarUrl)
                .onSuccess { user ->
                    _currentUser.value = user
                    // Note: Email verification might be required
                    onSuccess()
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Sign up failed"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Sign out the current user.
     */
    fun signOut() {
        viewModelScope.launch {
            BooktopiaRepository.signOut()
            _currentUser.value = null
            _userProfile.value = null
            _isLoggedIn.value = false
        }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Check if username is available.
     */
    suspend fun checkUsernameAvailable(username: String): Boolean {
        return BooktopiaRepository.isUsernameAvailable(username)
    }
}
