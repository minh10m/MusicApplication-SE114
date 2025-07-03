package com.example.musicapplicationse114.ui.screen.playlists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.PlaylistResponse
import com.example.musicapplicationse114.model.SessionCacheHandler
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayListUiState(
    val playlist : List<PlaylistResponse> = emptyList(),
    val searchPlaylist: List<PlaylistResponse> = emptyList(),
    val playlistCount : Int = 0,
    val status : LoadStatus = LoadStatus.Init(),
    val query : String = "",
    val success : String = "",
    val error : String = ""
)

@HiltViewModel
class PlayListViewModel @Inject constructor(
    private val api: Api?,
    private val tokenManager: TokenManager
) : ViewModel(), SessionCacheHandler {

    private val _uiState = MutableStateFlow(PlayListUiState())
    val uiState: StateFlow<PlayListUiState> = _uiState.asStateFlow()

    // Dùng trực tiếp List<PlaylistResponse> làm cache
    private val _playlists = MutableStateFlow<List<PlaylistResponse>>(emptyList())

    private var searchJob: Job? = null

    override fun hasSessionCache(): Boolean = _playlists.value.isNotEmpty()

    override fun clearSessionCache() {
        _playlists.value = emptyList()
    }

    fun updateSuccess(success: String) {
        _uiState.value = _uiState.value.copy(success = success)
    }

    fun updateError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    fun loadPlaylist(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && hasSessionCache()) {
                _uiState.value = _uiState.value.copy(
                    playlist = _playlists.value,
                    playlistCount = _playlists.value.size,
                    status = LoadStatus.Success()
                )
                return@launch
            }

            // 👉 Chỉ update status, giữ lại các field khác như success, error, query
            _uiState.value = _uiState.value.copy(
                status = LoadStatus.Loading()
            )

            try {
                val token = tokenManager.getToken()
                if (api != null && !token.isNullOrBlank()) {
                    val result = api.getMyPlaylists(token)
                    Log.d("PlaylistViewModel", "API Response: ${result.body()}")
                    if (result.isSuccessful && result.body() != null) {
                        val playlists = result.body()!!.content
                        _playlists.value = playlists

                        _uiState.value = _uiState.value.copy(
                            playlist = playlists,
                            playlistCount = playlists.size,
                            status = LoadStatus.Success()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            status = LoadStatus.Error("API error: ${result.code()}")
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Không có API hoặc token")
                    )
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(e.message.toString())
                )
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            try {
                val token = tokenManager.getToken()
                if (api != null && !token.isNullOrBlank()) {
                    val result = api.deletePlaylist(token, playlistId)
                    if (result.isSuccessful) {
                        // Cập nhật cache
                        _playlists.value = _playlists.value.filterNot { it.id == playlistId }

                        _uiState.value = _uiState.value.copy(
                            playlist = _playlists.value,
                            playlistCount = _playlists.value.size,
                            status = LoadStatus.Success()
                        )

                        updateSuccess("Xoá playlist thành công")
                    } else {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("API error: ${result.code()}"))
                        updateError("Xoá playlist thất bại")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có API hoặc token"))
                    updateError("Xoá playlist thất bại")
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
                updateError("Lỗi máy chủ, vui lòng thử lại")
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun searchAllDebounced(query: String, page: Int = 0, size: Int = 20) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(100)
            searchPlaylist(query, page, size)
        }
    }

    fun searchPlaylist(query: String, page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        searchPlaylist = emptyList(),
                        status = LoadStatus.Success()
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())

                val token = tokenManager.getToken()
                if (api != null && !token.isNullOrBlank()) {
                    val result = api.searchPlaylists(token, query, page, size)
                    Log.d("PlaylistViewModel", "API Search Response: ${result.body()}")
                    if (result.isSuccessful && result.body() != null) {
                        _uiState.value = _uiState.value.copy(
                            searchPlaylist = result.body()!!.content,
                            status = LoadStatus.Success()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("API error: ${result.code()}"))
                    }
                } else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Không có API hoặc token"))
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message.toString()))
            }
        }
    }
}
