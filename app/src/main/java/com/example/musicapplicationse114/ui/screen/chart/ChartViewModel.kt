package com.example.musicapplicationse114.ui.screen.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.SessionCacheHandler
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChartUiState(
    val songs : List<SongResponse> = emptyList(),
    val status : LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val api: Api?,
    private val tokenManager: TokenManager?
) : ViewModel(), SessionCacheHandler {
    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState = _uiState

    private var isLoaded = false

    override fun hasSessionCache(): Boolean = isLoaded

    override fun clearSessionCache() {
        isLoaded = false
        _uiState.value = ChartUiState() // Reset UI state
    }

    fun loadSongByTopViewCount(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.songs.isNotEmpty()) return@launch

            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if (!token.isNullOrBlank() && api != null) {
                    val songs = api.getTopSongByViewCount(token)
                    Log.d("ChartViewModel", "Songs loaded successfully")
                    _uiState.value = songs.body()?.content?.let {
                        _uiState.value.copy(
                            songs = it,
                            status = LoadStatus.Success()
                        )
                    } ?: _uiState.value.copy(status = LoadStatus.Error("Không có dữ liệu"))
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                Log.e("ChartViewModel", "Failed to load song: ${e.message}")
            }
        }
    }
}