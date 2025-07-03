package com.example.musicapplicationse114.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.model.NotificationDto
import com.example.musicapplicationse114.model.SessionCacheHandler
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val api: Api,
    private val tokenManager: TokenManager
) : ViewModel(), SessionCacheHandler {
    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _loadingState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadingState = _loadingState.asStateFlow()

    fun loadNotifications(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && hasSessionCache()) {
                return@launch
            }
            _loadingState.value = LoadState.Loading
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    val response = api.getMyNotifications(token)
                    if (response.isSuccessful) {
                        _notifications.value = response.body() ?: emptyList()
                        _loadingState.value = LoadState.Success
                    } else {
                        _loadingState.value = LoadState.Error("Failed to load notifications")
                    }
                } else {
                    _loadingState.value = LoadState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                _loadingState.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    override fun clearSessionCache() {
        _notifications.value = emptyList()
        _loadingState.value = LoadState.Idle
    }

    override fun hasSessionCache(): Boolean {
        return _notifications.value.isNotEmpty()
    }
}

sealed class LoadState {
    object Idle : LoadState()
    object Loading : LoadState()
    object Success : LoadState()
    data class Error(val message: String) : LoadState()
}