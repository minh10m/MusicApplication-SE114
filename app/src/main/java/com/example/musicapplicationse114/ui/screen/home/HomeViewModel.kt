package com.example.musicapplicationse114.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.common.enum.TimeOfDay
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.DownloadedSongResponse
import com.example.musicapplicationse114.model.FavoriteSongResponse
import com.example.musicapplicationse114.model.RecentlyPlayed
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.repositories.MainLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

data class HomeUiState(
    val albums: List<AlbumResponse> = emptyList(),
    val songs: List<SongResponse> = emptyList(),
    val recentPlayed: List<RecentlyPlayed> = emptyList(),
    val favoriteSongs: List<FavoriteSongResponse> = emptyList(),
    val downloadSongs: List<DownloadedSongResponse> = emptyList(),
    val status : LoadStatus = LoadStatus.Init(),
    val avatar : String = "",
    val username : String = "",
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: Api?,
    private val mainLog: MainLog?,
    private val tokenManager: TokenManager?
): ViewModel() {
    val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSong()
    {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if(api != null && !token.isNullOrBlank()){
                    Log.d("SongRequest", "Token: $token")
                    val songs = api.getSongs(token)
                    Log.d("HomeViewModel", "Songs loaded: ${songs.content.size} items")
                    _uiState.value = _uiState.value.copy(
                        songs = songs.content,
                        status = LoadStatus.Success()
                    )
                }
                else
                {
                    Log.e("HomeViewModel", "API hoặc token null")
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có token hoặc API"))
                }
            }catch(ex : Exception)
            {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(ex.message.toString()))
                Log.d("HomeViewModel", "Failed to load songs: ${ex.message}")
            }
        }
    }

    fun loadAlbum() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if (api != null && !token.isNullOrBlank()) {
                    Log.d("AlbumRequest", "Token: $token")
                    val albums = api.getAlbums(token)
                    Log.d("HomeViewModel", "Albums loaded: ${albums.content.size} items")
                    _uiState.value = _uiState.value.copy(
                        albums = albums.content,
                        status = LoadStatus.Success()
                    )
                } else {
                    Log.e("HomeViewModel", "API hoặc token null")
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có token hoặc API"))
                }
            } catch (ex: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(ex.message.toString()))
                Log.e("HomeViewModel", "Failed to load albums: ${ex.message}")
            }
        }
    }

    fun loadFavoriteSong() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())

            val token = tokenManager?.getToken()
            val userId = tokenManager?.getUserId()

            if (api == null || token.isNullOrBlank() || userId == null) {
                Log.e("HomeViewModel", "API, token, hoặc userId null")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Thiếu thông tin xác thực"))
                return@launch
            }

            try {
                Log.d("FavoriteSong", "Token: $token - UserId: $userId")

                val response = api.getFavoriteSongs(token, userId)
                if (response.isSuccessful) {
                    val songs = response.body()?.content.orEmpty()

                    _uiState.value = _uiState.value.copy(
                        favoriteSongs = songs,
                        status = LoadStatus.Success()
                    )

                    Log.d("HomeViewModel", "Favorite songs loaded: ${songs.size}")
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi không xác định"
                    Log.e("HomeViewModel", "Lỗi response: $errorMsg")
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi tải danh sách yêu thích"))
                }
            } catch (ex: Exception) {
                Log.e("HomeViewModel", "Exception: ${ex.message}", ex)
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi mạng hoặc máy chủ"))
            }
        }
    }
    fun loadDownloadedSong() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())

            val token = tokenManager?.getToken()
            val userId = tokenManager?.getUserId()

            if (api == null || token.isNullOrBlank() || userId == null) {
                Log.e("HomeViewModel", "API, token hoặc userId null khi load downloaded songs")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Thiếu thông tin xác thực"))
                return@launch
            }

            try {
                Log.d("DownloadedSongs", "Token: $token - UserId: $userId")

                val response = api.getDownloadedSongs(token, userId)
                if (response.isSuccessful) {
                    val downloadedSongs = response.body()?.content.orEmpty()

                    _uiState.value = _uiState.value.copy(
                        downloadSongs = downloadedSongs,
                        status = LoadStatus.Success()
                    )

                    Log.d("HomeViewModel", "Downloaded songs loaded: ${downloadedSongs.size}")
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi không xác định"
                    Log.e("HomeViewModel", "Lỗi response downloaded songs: $errorMsg")
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi tải bài hát đã tải xuống"))
                }
            } catch (ex: Exception) {
                Log.e("HomeViewModel", "Exception khi load downloaded songs: ${ex.message}", ex)
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi mạng hoặc máy chủ"))
            }
        }
    }



//    fun loadRecentPlayed()
//    {
//        viewModelScope.launch {
//            try {
//                if(api != null){
//                    val recentPlayed = api.loadRecentPlayed()
//                    _uiState.value = _uiState.value.copy(recentPlayed = recentPlayed, status = LoadStatus.Success())
//                }
//            }catch (ex: Exception)
//            {
//                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(ex.message.toString()))
//            }
//        }
//    }

    fun setTimeOfDay(){
        val hour = LocalDateTime.now().hour
        val timeOfDay = when(hour){
            in 5..11 -> TimeOfDay.MORNING
            in 12..17 -> TimeOfDay.AFTERNOON
            else -> TimeOfDay.EVENING
        }
        _uiState.value = _uiState.value.copy(timeOfDay = timeOfDay)
    }

    fun updateUserName(username: String){
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun getUserName(): String{
        return _uiState.value.username
    }

    fun getTimeOfDay() : TimeOfDay{
        return _uiState.value.timeOfDay
    }
}