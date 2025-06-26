package com.example.musicapplicationse114.ui.screen.player

import android.util.Log
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.model.AddFavoriteSongRequest
import com.example.musicapplicationse114.model.FavoriteSongResponse
import com.example.musicapplicationse114.model.DownloadedSongResponse
import com.example.musicapplicationse114.repositories.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val api: Api,
    private val tokenManager: TokenManager
) {
    private val _favoriteSongs = MutableStateFlow<List<FavoriteSongResponse>>(emptyList())
    val favoriteSongs: StateFlow<List<FavoriteSongResponse>> = _favoriteSongs.asStateFlow()

    private val _downloadedSongs = MutableStateFlow<List<DownloadedSongResponse>>(emptyList())
    val downloadedSongs: StateFlow<List<DownloadedSongResponse>> = _downloadedSongs.asStateFlow()

    // Cung cấp likedSongIds và downloadedSongIds cho PlayerViewModel
    val likedSongIds: StateFlow<Set<Long>>
        get() = MutableStateFlow(_favoriteSongs.value.map { it.song.id }.toSet()).asStateFlow()

    val downloadedSongIds: StateFlow<Set<Long>>
        get() = MutableStateFlow(_downloadedSongs.value.map { it.songId }.toSet()).asStateFlow()

    suspend fun refreshLikedSongs() {
        // Gọi getToken() và getUserId() trong cùng coroutine scope
        val token = tokenManager.getToken()
        if (token.isEmpty()) {
            Log.e("SongRepository", "No token available")
            return
        }
        val userId = tokenManager.getUserId()
        if (userId == -1L) {
            Log.e("SongRepository", "No userId available")
            return
        }

        try {
            val response = api.getFavoriteSongs(token)
            if (response.isSuccessful) {
                _favoriteSongs.value = response.body()?.content.orEmpty()
                Log.d("SongRepository", "Loaded favorite songs: ${_favoriteSongs.value.size}")
            } else {
                Log.e("SongRepository", "Failed to load favorite songs: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Failed to load favorite songs: ${e.message}")
        }
    }

    suspend fun refreshDownloadedSongs() {
        // Gọi getToken() và getUserId() trong cùng coroutine scope
        val token = tokenManager.getToken()
        if (token.isEmpty()) {
            Log.e("SongRepository", "No token available")
            return
        }
        val userId = tokenManager.getUserId()
        if (userId == -1L) {
            Log.e("SongRepository", "No userId available")
            return
        }

        try {
            val response = api.getDownloadedSongs(token)
            if (response.isSuccessful) {
                _downloadedSongs.value = response.body()?.content.orEmpty()
                Log.d("SongRepository", "Loaded downloaded songs: ${_downloadedSongs.value.size}")
            } else {
                Log.e("SongRepository", "Failed to load downloaded songs: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Failed to load downloaded songs: ${e.message}")
        }
    }

    suspend fun toggleFavorite(songId: Long) {
        // Gọi getToken() và getUserId() trong cùng coroutine scope
        val token = tokenManager.getToken()
        if (token.isEmpty()) {
            Log.e("SongRepository", "No token available")
            return
        }
        val userId = tokenManager.getUserId()
        if (userId == -1L) {
            Log.e("SongRepository", "No userId available")
            return
        }

        val currentFavorites = _favoriteSongs.value
        val isLiked = currentFavorites.any { it.song.id == songId }

        if (isLiked) {
            val favorite = currentFavorites.firstOrNull { it.song.id == songId }
            if (favorite != null) {
                try {
                    api.removeFavoriteSong(token, favorite.id)
                    _favoriteSongs.value = currentFavorites.filter { it.song.id != songId }
                    Log.d("SongRepository", "Removed favorite song: $songId")
                } catch (e: Exception) {
                    Log.e("SongRepository", "Failed to remove favorite song: ${e.message}")
                }
            }
        } else {
            try {
                api.addFavoriteSong(token, AddFavoriteSongRequest(songId))
                // Tải lại danh sách yêu thích để lấy ID và addedAt mới
                refreshLikedSongs()
                Log.d("SongRepository", "Added favorite song: $songId")
            } catch (e: Exception) {
                Log.e("SongRepository", "Failed to add favorite song: ${e.message}")
            }
        }
    }

    suspend fun toggleDownload(songId: Long) {
        // Gọi getToken() và getUserId() trong cùng coroutine scope
        val token = tokenManager.getToken()
        if (token.isEmpty()) {
            Log.e("SongRepository", "No token available")
            return
        }
        val userId = tokenManager.getUserId()
        if (userId == -1L) {
            Log.e("SongRepository", "No userId available")
            return
        }

        val currentDownloads = _downloadedSongs.value
        val isDownloaded = currentDownloads.any { it.songId == songId }

        if (isDownloaded) {
            val download = currentDownloads.firstOrNull { it.songId == songId }
            if (download != null) {
                try {
                    api.removeDownloadedSong(token, download.id)
                    _downloadedSongs.value = currentDownloads.filter { it.songId != songId }
                    Log.d("SongRepository", "Removed downloaded song: $songId")
                } catch (e: Exception) {
                    Log.e("SongRepository", "Failed to remove downloaded song: ${e.message}")
                }
            }
        } else {
            try {
                api.addDownloadedSong(token, songId)
                // Tải lại danh sách tải về để lấy ID và downloadedAt mới
                refreshDownloadedSongs()
                Log.d("SongRepository", "Added downloaded song: $songId")
            } catch (e: Exception) {
                Log.e("SongRepository", "Failed to add downloaded song: ${e.message}")
            }
        }
    }
}