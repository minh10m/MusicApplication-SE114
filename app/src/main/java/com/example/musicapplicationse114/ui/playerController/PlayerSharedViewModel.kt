package com.example.musicapplicationse114.ui.playerController

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.RecentlyPlayedRequest
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.ui.screen.player.PlayerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerSharedViewModel @Inject constructor(
    val player: GlobalPlayerController,
    val api: Api?,
    val tokenManager: TokenManager?
) : ViewModel() {
    private val _queue = mutableStateListOf<SongResponse>()
    val queue: List<SongResponse> get() = _queue


    private val _currentIndex = mutableStateOf(0)
    val currentIndex: State<Int> get() = _currentIndex

    val currentSong: SongResponse?
        get() = _queue.getOrNull(_currentIndex.value)

    fun setSongList(songs: List<SongResponse>, startIndex: Int = 0) {
        _queue.clear()
        _queue.addAll(songs)
        _currentIndex.value = startIndex
        player.setSongList(songs, startIndex)
    }

    fun resetPlayer() {
        _queue.clear()
        _currentIndex.value = 0
        player.stop() // Gọi hàm stop từ GlobalPlayerController
        Log.d("PlayerSharedViewModel", "MiniPlayer reset")
    }

    fun addRecentlyPlayed(songId: Long) {
        viewModelScope.launch(NonCancellable) {
            repeat(3) { attempt ->
                try {
                    val token = tokenManager?.getToken()?.takeIf { it.isNotBlank() }
                    val userId = tokenManager?.getUserId()?.takeIf { it != -1L }
                    Log.d("RecentPlayed", "Attempt $attempt: token=$token, userId=$userId, api=${api != null}, songId=$songId")

                    if (!token.isNullOrBlank() && api != null && userId != null) {
                        val response = api.addRecentlyPlayed(token, songId)

                        if (response.isSuccessful) {
                            Log.e("RecentPlayed", "Success on attempt $attempt")
                            return@launch
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e(
                                "RecentPlayed",
                                "Failed on attempt $attempt: HTTP ${response.code()} - $errorBody"
                            )
                        }
                    } else {
                        Log.e("RecentPlayed", "Missing token or userId or api is null")
                    }
                    delay(1000) // Chờ trước khi thử lại
                } catch (e: Exception) {
                    Log.e("RecentPlayed", "Exception on attempt $attempt: ${e.message}", e)
                }
            }
            Log.e("RecentPlayed", "Failed to add songId $songId after 3 attempts")
        }
    }

    init {
        // Lắng nghe trạng thái của GlobalPlayerController
        viewModelScope.launch {
            player.state.collectLatest { state ->
                state.currentSong?.let { currentSong ->
                    val index = player.getSongList().indexOfFirst { it.id == currentSong.id }
                    if (index != -1 && index != _currentIndex.value) {
                        setSongList(player.getSongList(), index)
                    }
                }
            }
        }
    }

    fun getUpNext(): SongResponse? {
        return _queue.getOrNull(_currentIndex.value + 1)
    }
}