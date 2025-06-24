package com.example.musicapplicationse114.ui.screen.searchtype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.musicapplicationse114.R
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.repositories.MainLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchTypeUiState(
    val recentSearches: List<RecentSearch> = emptyList(),
    val query: String = "",
    val songs: List<SongResponse> = emptyList(),
    val albums : List<AlbumResponse> = emptyList(),
    val artists: List<ArtistResponse> = emptyList(),
    val status : LoadStatus = LoadStatus.Init(),

    )

@HiltViewModel
class SearchTypeViewModel @Inject constructor(
    private val mainLog: MainLog?,
    private val api: Api?,
    private val tokenManager: TokenManager?
) : ViewModel() {
    val _uiState = MutableStateFlow(SearchTypeUiState())
    val uiState = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun searchAllDebounced(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(100)
            searchAll(query)
        }
    }


    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun searchAll(query: String)
    {
        if(query.isBlank()) return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if(api != null && !token.isNullOrBlank())
                {
                    val songs = api.searchSongs(token, query)
                    val albums = api.searchAlbums(token, query)
                    val artists = api.searchArtists(token, query)
                    _uiState.value = _uiState.value.copy(songs = songs.content, albums = albums.content, artists = artists.content, status = LoadStatus.Success())
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
