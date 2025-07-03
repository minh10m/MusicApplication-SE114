package com.example.musicapplicationse114.ui.screen.likedsongs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.FavoriteSongResponse
import com.example.musicapplicationse114.model.SessionCacheHandler
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LikeSongUiState(
    val likedSongs: List<FavoriteSongResponse> = emptyList(),
    val likedSongsSearch: List<FavoriteSongResponse> = emptyList(),
    val status: LoadStatus = LoadStatus.Init(),
    val query: String = "",
)

@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    private val api: Api?,
    private val tokenManager: TokenManager?
): ViewModel(), SessionCacheHandler
{
    private val _uiState = MutableStateFlow(LikeSongUiState())
    val uiState: StateFlow<LikeSongUiState> = _uiState

    private var searchJob: Job? = null

    private var isLoaded = false

    override fun hasSessionCache(): Boolean = isLoaded

    override fun clearSessionCache() {
        isLoaded = false
        _uiState.value = LikeSongUiState()
    }


    fun searchAllDebounced(query: String, limit: Int = 10) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(100)
            searchLikeSongs(query)
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun searchLikeSongs(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        likedSongsSearch = emptyList(),
                        status = LoadStatus.Success()
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                Log.d("LikedSongsViewModel", "Token: $token")
                if (api != null && !token.isNullOrBlank()) {
                    val result = api.searchFavoriteSongs(token, query)
                    Log.d("LikedSongsViewModel", "API Response: ${result.body()}")
                    if (result.isSuccessful) {
                        _uiState.value = result.body()?.let {
                            _uiState.value.copy(
                                likedSongsSearch = it.content,
                                status = LoadStatus.Success()
                            )
                        } ?: _uiState.value.copy(status = LoadStatus.Error("Empty response"))
                    } else {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("API error: ${result.code()}"))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có API hoặc token"))
                }
            } catch (e: Exception) {
                Log.e("LikedSongsViewModel", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
            }
        }
    }

    fun loadFavoriteSong(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isLoaded && !forceRefresh) return@launch

            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())

            val token = tokenManager?.getToken()
            val userId = tokenManager?.getUserId()

            if (api == null || token.isNullOrBlank() || userId == null) {
                Log.e("LikedSongsViewModel", "API, token, hoặc userId null")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Thiếu thông tin xác thực"))
                return@launch
            }

            try {
                Log.d("FavoriteSong", "Token: $token - UserId: $userId")

                val response = api.getFavoriteSongs(token)
                if (response.isSuccessful) {
                    val songs = response.body()?.content.orEmpty()
                    isLoaded = true // ✅ Đánh dấu cache đã có

                    _uiState.value = _uiState.value.copy(
                        likedSongs = songs,
                        status = LoadStatus.Success()
                    )

                    Log.d("LikedSongsViewModel", "Favorite songs loaded: ${songs.size}")
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi không xác định"
                    Log.e("LikedSongsViewModel", "Lỗi response: $errorMsg")
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi tải danh sách yêu thích"))
                }
            } catch (ex: Exception) {
                Log.e("LikedSongsViewModel", "Exception: ${ex.message}", ex)
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Lỗi mạng hoặc máy chủ"))
            }
        }
    }
}