package com.example.musicapplicationse114.ui.screen.artist

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
data class ArtistUiState(
    val songArtist: List<SongResponse> = emptyList(),
    val artist: Response<ArtistResponse>? = null,
    val status: LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val api: Api?,
    private val mainLog: MainLog?,
    private val tokenManager: TokenManager?
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState = _uiState

    fun loadArtistById(artistId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if (!token.isNullOrBlank() && api != null) {
                    val artist = api.getArtistById(token, artistId)

                    _uiState.value = _uiState.value.copy(
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

    fun loadSongByArtistId(artistId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if(!token.isNullOrBlank() && api != null)
                {
                    val songs = api.getSongsByArtistId(token, artistId)
                    Log.d("PlayerViewModel", "Songs loaded successfully")
                    _uiState.value = _uiState.value.copy(
                        songArtist = songs.content,
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
