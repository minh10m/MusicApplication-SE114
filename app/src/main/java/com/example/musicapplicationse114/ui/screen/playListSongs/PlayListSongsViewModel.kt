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
                    val response = api.getPlaylistById(token, playlistId)

                    if (response.isSuccessful) {
                        val playlist = response.body()

                        _uiState.value = _uiState.value.copy(
                            playlist = playlist,
                            status = LoadStatus.Success()
                        )
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        Log.e("PlayListSongsViewModel", "API error ${response.code()}: $errorMessage")
                        _uiState.value = _uiState.value.copy(
                            status = LoadStatus.Error("API error: ${response.code()}")
                        )
                    }
                } else {
                    Log.e("PlayListSongsViewModel", "Token hoặc API null")
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                }
            } catch (e: Exception) {
                Log.e("PlayListSongsViewModel", "Exception: ${e.message}")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun loadPlaylistWithSong(playlistId: Long)
    {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if (!token.isNullOrBlank() && api != null) {
                    val response = api.getPlaylistWithSongs(token, playlistId)
                    if(response.isSuccessful)
                    {
                        val playlist = response.body()

                        val songs = playlist?.songPlaylists?.mapNotNull {
                            it.song
                        } ?: emptyList()

                        Log.d("PlayListSongsViewModel", "Playlist and songs loaded successfully")
                        Log.d("PlayListSongsViewModel", "Playlist name: ${playlist?.name}")
                        Log.d("PlayListSongsViewModel", "Total songs: ${songs.size}")
                        playlist?.songPlaylists?.forEachIndexed { index, sp ->
                            Log.d("PlayListSongsViewModel", "[$index] Song: ${sp.song.title}")
                        }

                        _uiState.value = _uiState.value.copy(
                            songs = songs,
                            status = LoadStatus.Success()
                        )
                    }
                    else
                    {
                        val errorMessage = response.errorBody()?.string()
                        Log.e("PlayListSongsViewModel", "API error ${response.code()}: $errorMessage")
                    }
                }
                else
                {
                    Log.e("PlayListSongsViewModel", "Token hoặc API null")
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                }
            }
            catch (e : Exception) {
                Log.e("PlayListSongsViewModel", "Exception: ${e.message}")
                _uiState.value =
                    _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
            }
        }
    }

}