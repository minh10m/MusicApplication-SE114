package com.example.musicapplicationse114.ui.screen.player

import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.AddFavoriteSongRequest
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val song: SongResponse? = null,
    val playlist: List<SongResponse> = emptyList(),
    val currentIndex: Int = -1,
    val likedSongIds: Set<Long> = emptySet(),
    val downloadedSongIds: Set<Long> = emptySet(),
    val status: LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val api: Api?,
    private val tokenManager: TokenManager?
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSongById(songId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            try {
                val token = tokenManager?.getToken()
                val userId = tokenManager?.getUserId()
                if (!token.isNullOrBlank() && api != null && userId != null) {
                    val playlist = api.getSongs(token).content.orEmpty()
                    val index = playlist.indexOfFirst { it.id == songId }

                    val likedSongs = api.getFavoriteSongs(token, userId).body()?.content?.map { it.songId } ?: emptyList()
                    val downloadedSongs = api.getDownloadedSongs(token, userId).body()?.content?.map { it.songId } ?: emptyList()

                    if (index != -1) {
                        _uiState.value = PlayerUiState(
                            song = playlist[index],
                            playlist = playlist,
                            currentIndex = index,
                            likedSongIds = likedSongs.toSet(),
                            downloadedSongIds = downloadedSongs.toSet(),
                            status = LoadStatus.Success()
                        )
                        Log.d("PlayerViewModel", "Loaded song: ${playlist[index].title}")
                    } else {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không tìm thấy bài hát"))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                Log.e("PlayerViewModel", "Failed to load song: ${e.message}")
            }
        }
    }


    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            val token = tokenManager?.getToken()
            val userId = tokenManager?.getUserId()

            if (!token.isNullOrBlank() && api != null && userId != null) {
                val prevSet = _uiState.value.likedSongIds
                val currentSet = prevSet.toMutableSet()
                val isLiked = currentSet.contains(songId)

                // Cập nhật UI ngay lập tức
                if (isLiked) currentSet.remove(songId) else currentSet.add(songId)
                _uiState.value = _uiState.value.copy(likedSongIds = currentSet)

                if (isLiked) {
                    try {
                        val favoriteId = api.getFavoriteSongs(token, userId)
                            .body()?.content?.firstOrNull { it.songId == songId }?.id

                        if (favoriteId != null) {
                            api.removeFavoriteSong(token, favoriteId)
                            // Nếu thành công thì không làm gì thêm
                        } else {
                            throw Exception("Không tìm thấy favoriteId để xóa")
                        }
                    } catch (e: Exception) {
                        Log.e("FavoriteError", "Gỡ yêu thích thất bại: ${e.message}")
                        // Rollback UI nếu thật sự fail
                        _uiState.value = _uiState.value.copy(likedSongIds = prevSet)
                    }
                } else {
                    try {
                        val existed = api.getFavoriteSongs(token, userId)
                            .body()?.content?.any { it.songId == songId } ?: false

                        if (!existed) {
                            api.addFavoriteSong(token, AddFavoriteSongRequest(userId, songId))
                        } else {
                            Log.d("FavoriteSong", "Đã tồn tại trong danh sách, không thêm lại.")
                        }
                    } catch (e: Exception) {
                        Log.e("FavoriteError", "Thêm yêu thích thất bại: ${e.message}")
                        // Rollback UI nếu thật sự fail
                        _uiState.value = _uiState.value.copy(likedSongIds = prevSet)
                    }
                }
            }
        }
    }



    fun toggleDownload(songId: Long) {
        viewModelScope.launch {
            val token = tokenManager?.getToken()
            val userId = tokenManager?.getUserId()

            if (!token.isNullOrBlank() && api != null && userId != null) {
                val prevSet = _uiState.value.downloadedSongIds
                val currentSet = prevSet.toMutableSet()
                val isDownloaded = currentSet.contains(songId)

                // Cập nhật UI ngay lập tức
                if (isDownloaded) currentSet.remove(songId) else currentSet.add(songId)
                _uiState.value = _uiState.value.copy(downloadedSongIds = currentSet)

                if (isDownloaded) {
                    try {
                        val downloadedId = api.getDownloadedSongs(token, userId)
                            .body()?.content?.firstOrNull { it.songId == songId }?.id

                        if (downloadedId != null) {
                            api.removeDownloadedSong(token, downloadedId)
                        } else {
                            throw Exception("Không tìm thấy downloadedId để xóa")
                        }
                    } catch (e: Exception) {
                        Log.e("DownloadError", "Gỡ tải xuống thất bại: ${e.message}")
                        _uiState.value = _uiState.value.copy(downloadedSongIds = prevSet)
                    }
                } else {
                    try {
                        val existed = api.getDownloadedSongs(token, userId)
                            .body()?.content?.any { it.songId == songId } ?: false

                        if (!existed) {
                            api.addDownloadedSong(token, userId, songId)
                        } else {
                            Log.d("DownloadSong", "Đã tồn tại trong danh sách tải, không thêm lại.")
                        }
                    } catch (e: Exception) {
                        Log.e("DownloadError", "Tải bài hát thất bại: ${e.message}")
                        _uiState.value = _uiState.value.copy(downloadedSongIds = prevSet)
                    }
                }
            }
        }
    }




    fun nextSong() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex < state.playlist.size) {
            _uiState.value = state.copy(
                currentIndex = nextIndex,
                song = state.playlist[nextIndex]
            )
        }
    }

    fun previousSong() {
        val state = _uiState.value
        val prevIndex = state.currentIndex - 1
        if (prevIndex >= 0) {
            _uiState.value = state.copy(
                currentIndex = prevIndex,
                song = state.playlist[prevIndex]
            )
        }
    }
}