package com.example.musicapplicationse114.ui.screen.playListSongs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.PlaylistResponse
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayListSongsUiState(
    val songs: List<SongResponse> = emptyList(),
    val status: LoadStatus = LoadStatus.Init(),
    val playlist: PlaylistResponse? = null
)


@HiltViewModel
class PlayListSongsViewModel @Inject constructor(
     val api : Api?,
     val tokenManager: TokenManager?
) : ViewModel()
{
    private val _uiState = MutableStateFlow(PlayListSongsUiState())
    val uiState : StateFlow<PlayListSongsUiState> = _uiState

    fun loadPlaylistById(playlistId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if (!token.isNullOrBlank() && api != null) {
                    val playlist = api.getPlaylistById(token, playlistId)

                    _uiState.value = _uiState.value.copy(
                        playlist = playlist.body(),
                        songs = playlist.body()?.songPlaylists?.map {it.song} ?: emptyList(),
                        status = LoadStatus.Success()
                    )
                    if(playlist.isSuccessful)
                    {
                        Log.d("PlayListSongsViewModel", "Playlist and songs loaded successfully")
                    }
                }
                else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                Log.e("PlayerViewModel", "Failed to load song: ${e.message}")
            }
        }
    }
}