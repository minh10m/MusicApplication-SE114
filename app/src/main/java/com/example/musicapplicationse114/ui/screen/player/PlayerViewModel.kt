package com.example.musicapplicationse114.ui.screen.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val song: SongResponse? = null,
    val playlist: List<SongResponse> = emptyList(),
    val currentIndex: Int = -1,
    val status: LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val api: Api?,
    private val tokenManager: TokenManager?
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSongById(songId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            try {
                val token = tokenManager?.getToken()
                if (!token.isNullOrBlank() && api != null) {
                    // Lấy danh sách toàn bộ bài hát (playlist) một lần
                    val playlist = api.getSongs(token).content.orEmpty()
                    val index = playlist.indexOfFirst { it.id == songId }

                    if (index != -1) {
                        _uiState.value = PlayerUiState(
                            song = playlist[index],
                            playlist = playlist,
                            currentIndex = index,
                            status = LoadStatus.Success()
                        )
                        Log.d("PlayerViewModel", "Loaded song: ${playlist[index].title}")
                    } else {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không tìm thấy bài hát"))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                Log.e("PlayerViewModel", "Failed to load song: ${e.message}")
            }
        }
    }

    fun nextSong() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex < state.playlist.size) {
            _uiState.value = state.copy(
                currentIndex = nextIndex,
                song = state.playlist[nextIndex]
            )
        }
    }

    fun previousSong() {
        val state = _uiState.value
        val prevIndex = state.currentIndex - 1
        if (prevIndex >= 0) {
            _uiState.value = state.copy(
                currentIndex = prevIndex,
                song = state.playlist[prevIndex]
            )
        }
    }
}
