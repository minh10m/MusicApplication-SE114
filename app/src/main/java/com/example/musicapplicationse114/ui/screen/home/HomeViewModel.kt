package com.example.musicapplicationse114.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.common.enum.TimeOfDay
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.DownloadedSongResponse
import com.example.musicapplicationse114.model.FavoriteSongResponse
import com.example.musicapplicationse114.model.GenreResponse
import com.example.musicapplicationse114.model.RecentlyPlayedResponse
import com.example.musicapplicationse114.model.SessionCacheHandler
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
    val songsByGenre: Map<Long, List<SongResponse>> = emptyMap(),
    val likeCount : Int = 0,
    val downloadCount : Int = 0,
    val artists : List<ArtistResponse> = emptyList(),
    val genres : List<GenreResponse> = emptyList(),
    val recentPlayed: List<RecentlyPlayedResponse> = emptyList(),
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
): ViewModel(), SessionCacheHandler {
    val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSong(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Nếu đã có cache và không ép làm mới thì không gọi lại API
            if (!forceRefresh && _uiState.value.songs.isNotEmpty()) {
                Log.d("HomeViewModel", "Using cached songs")
                return@launch
            }

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())

            val token = tokenManager?.getToken()

            if (api != null && !token.isNullOrBlank()) {
                try {
                    Log.d("SongRequest", "Token: $token")

                    val songs = api.getSongs(token)

                    Log.d("HomeViewModel", "Songs loaded: ${songs.content.size} items")

                    _uiState.value = _uiState.value.copy(
                        songs = songs.content,
                        status = LoadStatus.Success()
                    )
                } catch (ex: Exception) {
                    Log.e("HomeViewModel", "Failed to load songs: ${ex.message}", ex)
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(ex.message ?: "Unknown error")
                    )
                }
            } else {
                Log.e("HomeViewModel", "API hoặc token null")
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error("Không có token hoặc API")
                )
            }
        }
    }

    fun loadGenre(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.genres.isNotEmpty()) {
                Log.d("HomeViewModel", "Using cached genres")
                return@launch
            }

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            val token = tokenManager?.getToken()

            if (api != null && !token.isNullOrBlank()) {
                try {
                    val response = api.getGenres(token)
                    val genres = response.body()?.content.orEmpty()
                    _uiState.value = _uiState.value.copy(
                        genres = genres,
                        status = LoadStatus.Success()
                    )
                    Log.d("HomeViewModel", "Genres loaded: ${genres.size} items")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
                    Log.e("HomeViewModel", "Failed to load genres: ${e.message}")
                }
            } else {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có token hoặc API"))
            }
        }
    }

    fun loadSongByGenre(genreId: Long, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.songsByGenre.containsKey(genreId)) {
                Log.d("HomeViewModel", "Using cached songs for genreId: $genreId")
                return@launch
            }

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            val token = tokenManager?.getToken()

            if (api != null && !token.isNullOrBlank()) {
                try {
                    val songs = api.getSongByGenreId(token, genreId)
                    _uiState.value = _uiState.value.copy(
                        songsByGenre = _uiState.value.songsByGenre + (genreId to songs.content),
                        status = LoadStatus.Success()
                    )
                    Log.d("HomeViewModel", "Songs loaded: ${songs.content.size} items for genre $genreId")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
                    Log.e("HomeViewModel", "Failed to load songs by genre: ${e.message}")
                }
            } else {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có token hoặc API"))
            }
        }
    }

    fun loadAlbum(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.albums.isNotEmpty()) {
                Log.d("HomeViewModel", "Using cached albums")
                return@launch
            }

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            val token = tokenManager?.getToken()

            if (api != null && !token.isNullOrBlank()) {
                try {
                    val albums = api.getAlbums(token)
                    _uiState.value = _uiState.value.copy(
                        albums = albums.content,
                        status = LoadStatus.Success()
                    )
                    Log.d("HomeViewModel", "Albums loaded: ${albums.content.size}")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
                    Log.e("HomeViewModel", "Failed to load albums: ${e.message}")
                }
            } else {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có token hoặc API"))
            }
        }
    }

    fun loadArtist(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.artists.isNotEmpty()) {
                Log.d("HomeViewModel", "Using cached artists")
                return@launch
            }

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            val token = tokenManager?.getToken()

            if (api != null && !token.isNullOrBlank()) {
                try {
                    val artists = api.getArtists(token)
                    _uiState.value = _uiState.value.copy(
                        artists = artists.content,
                        status = LoadStatus.Success()
                    )
                    Log.d("HomeViewModel", "Artists loaded: ${artists.content.size}")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
                    Log.e("HomeViewModel", "Failed to load artists: ${e.message}")
                }
            } else {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có token hoặc API"))
            }
        }
    }

    fun loadFavoriteSong(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.favoriteSongs.isNotEmpty()) {
                Log.d("HomeViewModel", "Using cached favorite songs")
                return@launch
            }

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            val token = tokenManager?.getToken()
            val userId = tokenManager?.getUserId()

            if (api == null || token.isNullOrBlank() || userId == null) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Thiếu thông tin xác thực"))
                return@launch
            }

            try {
                val response = api.getFavoriteSongs(token)
                if (response.isSuccessful) {
                    val songs = response.body()?.content.orEmpty()
                    _uiState.value = _uiState.value.copy(
                        favoriteSongs = songs,
                        likeCount = songs.size,
                        status = LoadStatus.Success()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi tải danh sách yêu thích"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi mạng hoặc máy chủ"))
            }
        }
    }

    fun loadDownloadedSong(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.downloadSongs.isNotEmpty()) {
                Log.d("HomeViewModel", "Using cached downloaded songs")
                return@launch
            }

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            val token = tokenManager?.getToken()
            val userId = tokenManager?.getUserId()

            if (api == null || token.isNullOrBlank() || userId == null) {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Thiếu thông tin xác thực"))
                return@launch
            }

            try {
                val response = api.getDownloadedSongs(token)
                if (response.isSuccessful) {
                    val downloadedSongs = response.body()?.content.orEmpty()
                    _uiState.value = _uiState.value.copy(
                        downloadSongs = downloadedSongs,
                        downloadCount = downloadedSongs.size,
                        status = LoadStatus.Success()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi tải bài hát đã tải xuống"))
                }
            } catch (e: Exception) {
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

    suspend fun updateUserName(){
        _uiState.value = tokenManager?.getUserName()?.let { _uiState.value.copy(username = it) }!!
    }

    fun getUserName(): String{
        return _uiState.value.username
    }

    fun getTimeOfDay() : TimeOfDay{
        return _uiState.value.timeOfDay
    }



    fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.avatar.isNotEmpty()) {
                Log.d("HomeViewModel", "Using cached profile")
                return@launch
            }
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            val token = tokenManager?.getToken()
            if (api != null && !token.isNullOrBlank()) {
                try {
                    val response = api.getMyProfile(token)
                    if (response.isSuccessful) {
                        val profile = response.body()
                        _uiState.value = _uiState.value.copy(
                            avatar = profile?.avatar ?: "",
                            username = profile?.username ?: tokenManager?.getUserName() ?: "",
                            status = LoadStatus.Success()
                        )
                        Log.d("HomeViewModel", "Profile loaded: ${profile?.username}, avatar: ${profile?.avatar}")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            status = LoadStatus.Error("Failed to load profile")
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error(e.message ?: "Unknown error")
                    )
                    Log.e("HomeViewModel", "Failed to load profile: ${e.message}")
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error("No token or API")
                )
            }
        }
    }

    override fun clearSessionCache() {
        _uiState.value = HomeUiState()
    }

    override fun hasSessionCache(): Boolean {
        return _uiState.value.songs.isNotEmpty() ||
                _uiState.value.albums.isNotEmpty() ||
                _uiState.value.artists.isNotEmpty() ||
                _uiState.value.favoriteSongs.isNotEmpty() ||
                _uiState.value.downloadSongs.isNotEmpty() ||
                _uiState.value.avatar.isNotEmpty()
    }

}