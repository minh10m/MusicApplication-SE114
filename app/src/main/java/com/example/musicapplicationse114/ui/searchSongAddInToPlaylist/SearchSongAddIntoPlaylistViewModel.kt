package com.example.musicapplicationse114.ui.searchSongAddInToPlaylist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.SongPlaylistRequest
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Error
import javax.inject.Inject

data class SearchSongAddIntoPlaylistState(
    val status: LoadStatus = LoadStatus.Init(),
    val query : String = "",
    val songs : List<SongResponse> = emptyList(),
    val songsSearch: List<SongResponse> = emptyList(),
    val addedSongIds : Set<Long> = emptySet(),
    val playlistId : Long = 0,
    val successMessage: String = "",
    val addSong : Boolean = false,
    val error: String = ""
)

@HiltViewModel
class SearchSongAddIntoPlaylistViewModel @Inject constructor(
    private val api : Api?,
    private val tokenManager: TokenManager?,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchSongAddIntoPlaylistState())
    val uiState: StateFlow<SearchSongAddIntoPlaylistState> = _uiState


    private var searchJob: Job? = null

    fun searchAllDebounced(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(100)
            searchSongs(query)
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(status = LoadStatus.Init())
    }

    private fun updateSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    private fun updateError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    fun searchSongs(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        songsSearch = emptyList(),
                        status = LoadStatus.Success()
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                Log.d("SearchSongAddIntoPlaylistViewModel", "Token: $token")
                if (api != null && !token.isNullOrBlank()) {
                    val result = api.searchSongs(token, query)
                    Log.d("SearchSongAddIntoPlaylistViewModel", "API Response: ${result.body()?.content}")
                    if (result.isSuccessful) {
                        _uiState.value = result.body()?.let {
                            _uiState.value.copy(
                                songsSearch = it.content,
                                status = LoadStatus.Success()
                            )
                        } ?: _uiState.value.copy(status = LoadStatus.Error("Empty response"))
                    } else {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("API error: ${result.code()}"))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có API hoặc token"))
                }
            } catch (e: Exception) {
                Log.e("SearchSongAddIntoPlaylistViewModel", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
            }
        }
    }

    fun setPlaylistInfo(playlistId: Long, addedSongIds: Set<Long>) {
        _uiState.value = _uiState.value.copy(
            playlistId = playlistId,
            addedSongIds = addedSongIds
        )
        Log.d("SearchSongAddIntoPlaylistViewModel", "Playlist ID: $playlistId")
        Log.d("SearchSongAddIntoPlaylistViewModel", "Added Song IDs: $addedSongIds")
    }


    fun addSongToPlaylist(songId: Long, playlistId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                Log.d("SearchSongAddIntoPlaylistViewModel", "Token: $token")
                if (api != null && !token.isNullOrBlank()) {
                    val result = api.addSongToPlaylist(token, SongPlaylistRequest(songId, playlistId))
                    Log.d("SearchSongAddIntoPlaylistViewModel", "API Response: ${result.body()?.song?.title}")
                    if (result.isSuccessful) {
                        updateSuccessMessage("Thêm bài hát thành công")
                        Log.d("SearchSongAddIntoPlaylistViewModel", "Song added successfully")
                        _uiState.value = result.body()?.let {
                            _uiState.value.copy(
                                addedSongIds = _uiState.value.addedSongIds + songId,
                                addSong = true,
                                status = LoadStatus.Success()
                            )

                        } ?: _uiState.value.copy(status = LoadStatus.Error("Empty response"))
                    } else {
                        updateError("Thêm bài hát thất bại")
                        Log.e("SearchSongAddIntoPlaylistViewModel", "API error: ${result.code()}")
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("API error: ${result.errorBody()}"))
                    }
                } else {
                    updateError("Thêm bài hát thất bại")
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có API hoặc token"))
                }
            } catch (e: Exception) {
                updateError("Thêm bài hát thất bại")
                Log.e("SearchSongAddIntoPlaylistViewModel", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
            }
        }
    }

    fun loadSong()
    {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if(api != null && !token.isNullOrBlank()){
                    Log.d("SongRequest", "Token: $token")
                    val songs = api.getSongs(token)
                    Log.d("SearchSongAddIntoPlaylistViewModel", "Songs loaded: ${songs.content.size} items")
                    _uiState.value = _uiState.value.copy(
                        songs = songs.content,
                        status = LoadStatus.Success()
                    )
                }
                else
                {
                    Log.e("SearchSongAddIntoPlaylistViewModel", "API hoặc token null")
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có token hoặc API"))
                }
            }catch(ex : Exception)
            {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(ex.message.toString()))
                Log.d("SearchSongAddIntoPlaylistViewModel", "Failed to load songs: ${ex.message}")
            }
        }
    }
}