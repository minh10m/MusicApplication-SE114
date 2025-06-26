package com.example.musicapplicationse114.ui.screen.likedsongs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.FavoriteSongResponse
import com.example.musicapplicationse114.model.GenreResponse
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LikeSongUiState(
    val likedSongs: List<FavoriteSongResponse> = emptyList(),
    val likedSongsSearch: List<FavoriteSongResponse> = emptyList(),
    val status: LoadStatus = LoadStatus.Init(),
    val query: String = "",
)

@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    private val api: Api?,
    private val tokenManager: TokenManager?
): ViewModel()
{
    private val _uiState = MutableStateFlow(LikeSongUiState())
    val uiState: StateFlow<LikeSongUiState> = _uiState

    private var searchJob: Job? = null

    fun searchAllDebounced(query: String, limit: Int = 10) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(100)
            searchLikeSongs(query, limit)
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun searchLikeSongs(query: String, limit: Int = 10)
    {
        if(query.isBlank()) return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if(api != null && !token.isNullOrBlank())
                {
                    val result = api.searchFavoriteSongs(token, query, limit)
                    _uiState.value = result.body()?.let {
                        _uiState.value.copy(
                            likedSongsSearch = it.content,
                            status = LoadStatus.Success())
                    }!!
                }
                else
                {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có API"))
                }
            }
            catch(e: Exception)
            {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
            }
        }

    }
    
}