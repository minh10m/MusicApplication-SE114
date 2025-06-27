package com.example.musicapplicationse114.ui.screen.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.FollowArtistRequest
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
    val followedArtistIds: Set<Long> = emptySet(), // Thêm trường để lưu ID nghệ sĩ đã theo dõi
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
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                Log.e("ArtistViewModel", "Failed to load artist: ${e.message}")
            }
        }
    }

    fun loadSongByArtistId(artistId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if (!token.isNullOrBlank() && api != null) {
                    val songs = api.getSongsByArtistId(token, artistId)
                    Log.d("ArtistViewModel", "Songs loaded successfully: ${songs.content}")
                    _uiState.value = _uiState.value.copy(
                        songArtist = songs.content,
                        status = LoadStatus.Success()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                Log.e("ArtistViewModel", "Failed to load songs: ${e.message}")
            }
        }
    }

    fun loadFollowedArtists() {
        viewModelScope.launch {
            try {
                val token = tokenManager?.getToken()
                if (!token.isNullOrBlank() && api != null) {
                    val followedArtists = api.getFollowedArtists(token)
                    Log.d("ArtistViewModel", "Followed artists: ${followedArtists.body()?.content}")
                    if (followedArtists.isSuccessful) {
                        _uiState.value = _uiState.value.copy(
                            followedArtistIds = followedArtists.body()?.content?.map { it.artist.id }?.toSet() ?: emptySet(),
                            status = LoadStatus.Success()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("API error: ${followedArtists.code()}"))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("No API or token"))
                }
            } catch (e: Exception) {
                Log.e("ArtistViewModel", "Failed to load followed artists: ${e.message}")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun toggleFollowArtist(artistId: Long) {
        viewModelScope.launch {
            val token = tokenManager?.getToken()
            if (token.isNullOrBlank() || api == null) {
                Log.e("ArtistViewModel", "Missing token or API")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không thể xác thực"))
                return@launch
            }

            val prevSet = _uiState.value.followedArtistIds
            val currentSet = prevSet.toMutableSet()
            val isFollowed = currentSet.contains(artistId)

            // Cập nhật UI ngay lập tức
            if (isFollowed) currentSet.remove(artistId) else currentSet.add(artistId)
            _uiState.value = _uiState.value.copy(followedArtistIds = currentSet)

            try {
                if (isFollowed) {
                    val response = api.unfollowArtist(token, artistId)
                    Log.d("ArtistViewModel", "Unfollow response: ${response.code()}, isSuccessful=${response.isSuccessful}")
                    if (response.isSuccessful) {
                        Log.d("ArtistViewModel", "Đã bỏ theo dõi nghệ sĩ $artistId")
                        loadFollowedArtists() // Cập nhật danh sách
                    } else {
                        throw Exception("API unfollow thất bại: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                } else {
                    val existed = api.getFollowedArtists(token).body()?.content?.any { it.artist.id == artistId } ?: false
                    if (!existed) {
                        val response = api.followArtist(token, FollowArtistRequest(artistId))
                        if (response.isSuccessful) {
                            Log.d("ArtistViewModel", "Đã theo dõi nghệ sĩ $artistId")
                            loadFollowedArtists() // Cập nhật danh sách
                        } else {
                            throw Exception("API follow thất bại: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    } else {
                        Log.d("ArtistViewModel", "Nghệ sĩ $artistId đã được theo dõi, không thêm lại")
                    }
                }
            } catch (e: Exception) {
                Log.e("ArtistViewModel", "Thao tác theo dõi thất bại: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    followedArtistIds = prevSet,
                    status = LoadStatus.Error("Thao tác theo dõi thất bại: ${e.message}")
                )
            }
        }
    }
}