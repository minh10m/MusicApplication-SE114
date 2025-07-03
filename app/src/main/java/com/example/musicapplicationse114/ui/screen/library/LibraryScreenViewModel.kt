package com.example.musicapplicationse114.ui.screen.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.SessionCacheHandler
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.repositories.MainLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class LibraryUiState(
    val recentlyPlayed: List<SongResponse> = emptyList(),
    val status: LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mainLog : MainLog?,
    private val api : Api?,
    private val tokenManager: TokenManager?
) : ViewModel(), SessionCacheHandler {
    val _uiState = MutableStateFlow(LibraryUiState())
    val uiState =  _uiState.asStateFlow()
    private var isLoaded = false

    override fun hasSessionCache(): Boolean = isLoaded

    override fun clearSessionCache() {
        isLoaded = false
        _uiState.value = LibraryUiState()
    }

    fun loadRecentlyPlayed(forceRefresh: Boolean = false)
    {
        if (isLoaded && !forceRefresh) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            try {
                val token = tokenManager?.getToken()?.takeIf { it.isNotBlank() }
                val userId = tokenManager?.getUserId()?.takeIf { it != -1L }
                if (!token.isNullOrBlank() && api != null && userId != null)
                {
                    val response = api.getRecentlyPlayed(token)
                    if (response.isSuccessful)
                    {
                        _uiState.value = response.body()?.let {
                            _uiState.value.copy(
                                recentlyPlayed = it.content,
                                status = LoadStatus.Success()
                            )
                        }!!
                    }
                    else
                    {
                        val errorBody = response.errorBody()?.string()
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Failed: HTTP ${response.code()} - $errorBody"))
                        Log.e(
                            "LibraryViewModel",
                            "Failed: HTTP ${response.code()} - $errorBody"
                        )
                    }
                }
                else
                {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Missing token or userId or api is null"))
                    Log.e("LibraryViewModel", "Missing token or userId or api is null")
                }

            }
            catch(e:Exception)
            {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Lỗi không xác định"))
                    Log.e("LibraryViewModel", "Failed to load recently played: ${e.message}")
            }
        }
    }
}
