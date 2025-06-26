package com.example.musicapplicationse114.ui.screen.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.AddFavoriteSongRequest
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

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

    private var loadJob: Job? = null

    fun loadSongById(songId: Long) {
        loadJob?.cancel() // Hủy job trước nếu đang chạy
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            try {
                val token = tokenManager?.getToken()
                val userId = tokenManager?.getUserId()
                if (!token.isNullOrBlank() && api != null && userId != null) {
                    val playlist = api.getSongs(token).content.orEmpty()
                    val index = playlist.indexOfFirst { it.id == songId }

                    if (index != -1) {
                        val likedSongs = api.getFavoriteSongs(token).body()?.content?.map { it.song.id } ?: emptyList()
                        val downloadedSongs = api.getDownloadedSongs(token).body()?.content?.map { it.song.id } ?: emptyList()

                        _uiState.value = PlayerUiState(
                            song = playlist[index],
                            playlist = playlist,
                            currentIndex = index,
                            likedSongIds = likedSongs.toSet(),
                            downloadedSongIds = downloadedSongs.toSet(),
                            status = LoadStatus.Success()
                        )
                        Log.d("PlayerViewModel", "Loaded song: ${playlist[index].title}, likedSongIds=$likedSongs, downloadedSongIds=$downloadedSongs")
                    } else {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không tìm thấy bài hát"))
                        Log.w("PlayerViewModel", "Song with ID $songId not found in playlist")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                    Log.w("PlayerViewModel", "Missing token or API")
                }
            } catch (e: CancellationException) {
                Log.w("PlayerViewModel", "Coroutine bị hủy: ${e.message}")
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Lỗi không xác định"))
                Log.e("PlayerViewModel", "Failed to load song: ${e.message}")
            }
        }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            val token = tokenManager?.getToken()
            val userId = tokenManager?.getUserId()

            Log.d("FavoriteSong", "toggleFavorite called for songId=$songId, token=$token, userId=$userId, api=${api != null}")

            if (token.isNullOrBlank() || api == null || userId == null) {
                Log.e("FavoriteError", "Missing token, userId, or api")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không thể xác thực"))
                return@launch
            }

            val prevSet = _uiState.value.likedSongIds
            val currentSet = prevSet.toMutableSet()
            val isLiked = currentSet.contains(songId)

            Log.d("FavoriteSong", "isLiked=$isLiked, prevSet=$prevSet")

            // Cập nhật UI ngay lập tức
            if (isLiked) currentSet.remove(songId) else currentSet.add(songId)
            _uiState.value = _uiState.value.copy(likedSongIds = currentSet)

            try {
                if (isLiked) {
                    val favoriteSongs = api.getFavoriteSongs(token).body()?.content
                    val favoriteId = favoriteSongs?.firstOrNull { it.song.id == songId }?.id
                    Log.d("FavoriteSong", "favoriteId=$favoriteId, favoriteSongs=$favoriteSongs")

                    if (favoriteId != null) {
                        delay(1000) // Tăng thời gian chờ
                        val response = api.removeFavoriteSong(token, songId)
                        Log.d("FavoriteSong", "removeFavoriteSong response: ${response.code()}, isSuccessful=${response.isSuccessful}, body=${response.body()}")
                        if (response.isSuccessful) {
                            Log.d("FavoriteSong", "Đã xóa khỏi danh sách yêu thích")
                            delay(500)
                            val updatedLikedSongs = api.getFavoriteSongs(token)
                                .body()?.content?.map { it.song.id }?.toSet() ?: emptySet()
                            Log.d("FavoriteSong", "Updated likedSongIds=$updatedLikedSongs")
                            _uiState.value = _uiState.value.copy(likedSongIds = updatedLikedSongs)
                        } else {
                            throw Exception("API xóa thất bại: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    } else {
                        throw Exception("Không tìm thấy favoriteId để xóa trong danh sách mới")
                    }
                } else {
                    val existed = api.getFavoriteSongs(token)
                        .body()?.content?.any { it.song.id == songId } ?: false
                    if (!existed) {
                        val response = api.addFavoriteSong(token, AddFavoriteSongRequest(songId))
                        if (response.isSuccessful) {
                            Log.d("FavoriteSong", "Đã thêm vào danh sách yêu thích")
                            delay(500)
                            val updatedLikedSongs = api.getFavoriteSongs(token)
                                .body()?.content?.map { it.song.id }?.toSet() ?: emptySet()
                            Log.d("FavoriteSong", "Updated likedSongIds=$updatedLikedSongs")
                            _uiState.value = _uiState.value.copy(likedSongIds = updatedLikedSongs)
                        } else {
                            throw Exception("API thêm thất bại: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    } else {
                        Log.d("FavoriteSong", "Đã tồn tại trong danh sách, không thêm lại")
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoriteError", "Thao tác yêu thích thất bại: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    likedSongIds = prevSet,
                    status = LoadStatus.Error("Thao tác yêu thích thất bại: ${e.message}")
                )
            }
        }
    }

    fun toggleDownload(songId: Long) {
        viewModelScope.launch {
            val token = tokenManager?.getToken()
            val userId = tokenManager?.getUserId()

            Log.d("DownloadSong", "toggleDownload called for songId=$songId, token=$token, userId=$userId, api=${api != null}")

            if (token.isNullOrBlank() || api == null || userId == null) {
                Log.e("DownloadError", "Missing token, userId, or api")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không thể xác thực"))
                return@launch
            }

            val prevSet = _uiState.value.downloadedSongIds
            val currentSet = prevSet.toMutableSet()
            val isDownloaded = currentSet.contains(songId)

            Log.d("DownloadSong", "isDownloaded=$isDownloaded, prevSet=$prevSet")

            // Cập nhật UI ngay lập tức
            if (isDownloaded) currentSet.remove(songId) else currentSet.add(songId)
            _uiState.value = _uiState.value.copy(downloadedSongIds = currentSet)

            try {
                if (isDownloaded) {
                    val downloadedSongs = api.getDownloadedSongs(token).body()?.content
                    val downloadedId = downloadedSongs?.firstOrNull { it.song.id == songId }?.id
                    Log.d("DownloadSong", "downloadedId=$downloadedId, downloadedSongs=$downloadedSongs")

                    if (downloadedId != null) {
                        delay(1000) // Tăng thời gian chờ
                        val response = api.removeDownloadedSong(token, songId)
                        Log.d("DownloadSong", "removeDownloadedSong response: ${response.code()}, isSuccessful=${response.isSuccessful}, body=${response.body()}")
                        if (response.isSuccessful) {
                            Log.d("DownloadSong", "Đã xóa khỏi danh sách tải xuống")
                            delay(500)
                            val updatedDownloadedSongs = api.getDownloadedSongs(token)
                                .body()?.content?.map { it.song.id }?.toSet() ?: emptySet()
                            Log.d("DownloadSong", "Updated downloadedSongIds=$updatedDownloadedSongs")
                            _uiState.value = _uiState.value.copy(downloadedSongIds = updatedDownloadedSongs)
                        } else {
                            throw Exception("API xóa thất bại: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    } else {
                        throw Exception("Không tìm thấy downloadedId để xóa trong danh sách mới")
                    }
                } else {
                    val existed = api.getDownloadedSongs(token)
                        .body()?.content?.any { it.song.id == songId } ?: false
                    if (!existed) {
                        val response = api.addDownloadedSong(token, songId)
                        if (response.isSuccessful) {
                            Log.d("DownloadSong", "Đã thêm vào danh sách tải xuống")
                            delay(500)
                            val updatedDownloadedSongs = api.getDownloadedSongs(token)
                                .body()?.content?.map { it.song.id }?.toSet() ?: emptySet()
                            Log.d("DownloadSong", "Updated downloadedSongIds=$updatedDownloadedSongs")
                            _uiState.value = _uiState.value.copy(downloadedSongIds = updatedDownloadedSongs)
                        } else {
                            throw Exception("API thêm thất bại: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    } else {
                        Log.d("DownloadSong", "Đã tồn tại trong danh sách tải, không thêm lại")
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadError", "Thao tác tải xuống thất bại: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    downloadedSongIds = prevSet,
                    status = LoadStatus.Error("Thao tác tải xuống thất bại: ${e.message}")
                )
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