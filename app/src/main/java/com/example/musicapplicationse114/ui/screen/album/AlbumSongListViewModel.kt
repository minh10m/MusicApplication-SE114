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

    fun loadAlbumById(albumId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if (!token.isNullOrBlank() && api != null) {
                    val album = api.getAlbumById(token, albumId)
                    val artist = api.getArtistById(token, album.artistId)

                    _uiState.value = _uiState.value.copy(
                        album = album,
                        artist = artist,
                        status = LoadStatus.Success()
                    )
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

    fun loadSongByAlbumId(albumId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if(!token.isNullOrBlank() && api != null)
                {
                    val songs = api.getSongsByAlbumId(token, albumId)
                    Log.d("PlayerViewModel", "Songs loaded successfully")
                    _uiState.value = _uiState.value.copy(
                        songAlbums = songs.content,
                        status = LoadStatus.Success()
                )
            }
            else
            {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
            }
        }
        catch(e : Exception)
        {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
            Log.e("PlayerViewModel", "Failed to load song: ${e.message}")
            }

        }
    }
}
