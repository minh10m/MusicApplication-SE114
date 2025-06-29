package com.example.musicapplicationse114.ui.screen.playlists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.PlaylistResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayListUiState(
    val playlist : List<PlaylistResponse> = emptyList(),
    val searchPlaylist: List<PlaylistResponse> = emptyList(),
    val playlistCount : Int = 0,
    val status : LoadStatus = LoadStatus.Init(),
    val query : String = ""
)

@HiltViewModel
class PlayListViewModel @Inject constructor(
    private val api: Api?,
    private val tokenManager: TokenManager
) : ViewModel()
{
    private val _uiState = MutableStateFlow(PlayListUiState())
    val uiState : StateFlow<PlayListUiState> = _uiState

    private var searchJob: Job? = null

    fun loadPlaylist() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if (api != null && !token.isNullOrBlank()) {
                    val result = api.getMyPlaylists(token)
                    Log.d("PlaylistViewModel", "API Response: ${result.body()}")
                    if (result.isSuccessful) {
                        _uiState.value = _uiState.value.copy(
                            playlist = result.body()?.content ?: emptyList(),
                            playlistCount = (result.body()?.content ?: emptyList()).size,
                            status = LoadStatus.Success()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("API error: ${result.code()}"))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có API hoặc token"))
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
            }
        }
    }

    fun searchAllDebounced(query: String, page: Int = 0, size: Int = 20) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(100)
            searchPlaylist(query, page, size)
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun searchPlaylist(query: String, page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        searchPlaylist = emptyList(),
                        status = LoadStatus.Success()
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if (api != null && !token.isNullOrBlank()) {
                    val result = api.searchPlaylists(token, query, page, size)
                    Log.d("PlaylistViewModel", "API Search Response: ${result.body()}")
                    if (result.isSuccessful) {
                        _uiState.value = result.body()?.let {
                            _uiState.value.copy(
                                searchPlaylist = it.content,
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
                Log.e("PlaylistViewModel", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
            }
        }
    }
}