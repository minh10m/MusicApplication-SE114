package com.example.musicapplicationse114.ui.screen.album

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.repositories.MainLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
data class AlbumUiState(
    val songAlbums: List<SongResponse> = emptyList(),
    val album: AlbumResponse? = null,
    val artist: Response<ArtistResponse>? = null,
    val status: LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class AlbumSongListViewModel @Inject constructor(
    private val api: Api?,
    private val mainLog: MainLog?,
    private val tokenManager: TokenManager?
) : ViewModel() {
    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState = _uiState

    fun loadAlbumById(albumId: Long, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.album?.id == albumId && _uiState.value.artist?.isSuccessful == true) {
                Log.d("AlbumViewModel", "Using cached album and artist")
                return@launch
            }

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            val token = tokenManager?.getToken()

            if (!token.isNullOrBlank() && api != null) {
                try {
                    val album = api.getAlbumById(token, albumId)
                    val artist = api.getArtistById(token, album.artistId)

                    _uiState.value = _uiState.value.copy(
                        album = album,
                        artist = artist,
                        status = LoadStatus.Success()
                    )
                    Log.d("AlbumViewModel", "Loaded album and artist successfully")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                    Log.e("AlbumViewModel", "Failed to load album/artist: ${e.message}")
                }
            } else {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
            }
        }
    }

    fun loadSongByAlbumId(albumId: Long, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.songAlbums.isNotEmpty()) {
                Log.d("AlbumViewModel", "Using cached album songs")
                return@launch
            }

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            val token = tokenManager?.getToken()

            if (!token.isNullOrBlank() && api != null) {
                try {
                    val songs = api.getSongsByAlbumId(token, albumId)
                    _uiState.value = _uiState.value.copy(
                        songAlbums = songs.content,
                        status = LoadStatus.Success()
                    )
                    Log.d("AlbumViewModel", "Songs by album loaded successfully")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                    Log.e("AlbumViewModel", "Failed to load songs by album: ${e.message}")
                }
            } else {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
            }
        }
    }
}
