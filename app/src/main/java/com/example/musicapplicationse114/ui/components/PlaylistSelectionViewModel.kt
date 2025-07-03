package com.example.musicapplicationse114.ui.components

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.PlaylistResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.musicapplicationse114.model.SongPlaylistRequest
import javax.inject.Inject

data class PlaylistSelectionUiState(
    val playlists: List<PlaylistResponse> = emptyList(),
    val isLoading: Boolean = false,
    val status: LoadStatus = LoadStatus.Init(),
    val playlistsContainingSong: Set<Long> = emptySet(), // playlist IDs that contain the song
    val isCheckingPlaylists: Boolean = false
)

@HiltViewModel
class PlaylistSelectionViewModel @Inject constructor(
    private val api: Api,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistSelectionUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMyPlaylistsAndCheckSong(songId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Authentication token not found"),
                        isLoading = false
                    )
                    return@launch
                }

                Log.d("PlaylistSelectionViewModel", "Loading my playlists and checking song $songId")

                val response = api.getMyPlaylists(
                    token = token,
                    page = 0,
                    size = 100 // Get all playlists
                )

                Log.d("PlaylistSelectionViewModel", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val playlistPage = response.body()
                    val playlists = playlistPage?.content ?: emptyList()
                    Log.d("PlaylistSelectionViewModel", "Loaded ${playlists.size} playlists")
                    
                    // Check which playlists contain the song
                    val playlistsContainingSong = mutableSetOf<Long>()
                    
                    for (playlist in playlists) {
                        try {
                            val playlistWithSongs = api.getPlaylistWithSongs(token, playlist.id)
                            if (playlistWithSongs.isSuccessful) {
                                val playlistData = playlistWithSongs.body()
                                val containsSong = playlistData?.songPlaylists?.any { it.songId == songId } == true
                                if (containsSong) {
                                    playlistsContainingSong.add(playlist.id)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("PlaylistSelectionViewModel", "Error checking playlist ${playlist.id}", e)
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        playlists = playlists,
                        playlistsContainingSong = playlistsContainingSong,
                        status = LoadStatus.Success(),
                        isLoading = false
                    )
                } else {
                    Log.e("PlaylistSelectionViewModel", "Failed to load playlists: ${response.errorBody()?.string()}")
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Failed to load playlists: ${response.code()}"),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("PlaylistSelectionViewModel", "Error loading playlists", e)
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error("Error loading playlists: ${e.message}"),
                    isLoading = false
                )
            }
        }
    }

    fun addSongToSelectedPlaylists(
        songId: Long, 
        selectedPlaylistIds: List<Long>,
        onResult: (String) -> Unit // Callback for toast message
    ) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    onResult("Authentication token not found")
                    return@launch
                }

                var successCount = 0
                var errorCount = 0
                var alreadyExistsCount = 0
                val errors = mutableListOf<String>()
                
                for (playlistId in selectedPlaylistIds) {
                    try {
                        val response = api.addSongToPlaylist(
                            token = token,
                            request = SongPlaylistRequest(
                                songId = songId,
                                playlistId = playlistId
                            )
                        )

                        when (response.code()) {
                            200, 201 -> {
                                successCount++
                            }
                            409 -> {
                                alreadyExistsCount++
                            }
                            else -> {
                                errorCount++
                                val errorBody = response.errorBody()?.string()
                                val formattedError = formatBackendError(errorBody)
                                errors.add(formattedError)
                            }
                        }
                    } catch (e: Exception) {
                        errorCount++
                        errors.add("Network error: ${e.message}")
                    }
                }
                
                // Generate result message
                val message = buildString {
                    if (successCount > 0) {
                        append("Successfully added to $successCount playlist(s)")
                    }
                    if (alreadyExistsCount > 0) {
                        if (isNotEmpty()) append(". ")
                        append("Song already in $alreadyExistsCount playlist(s)")
                    }
                    if (errorCount > 0) {
                        if (isNotEmpty()) append(". ")
                        append("Failed to add to $errorCount playlist(s)")
                        if (errors.isNotEmpty()) {
                            append(": ${errors.first()}")
                        }
                    }
                }
                
                onResult(message.ifEmpty { "Operation completed" })
                
                // Reload to update checkbox states
                loadMyPlaylistsAndCheckSong(songId)
                
            } catch (e: Exception) {
                onResult("Error adding song to playlists: ${e.message}")
            }
        }
    }

    fun updateSongPlaylists(
        songId: Long,
        playlistsToAdd: List<Long>,
        playlistsToRemove: List<Long>,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    onResult("Authentication token not found")
                    return@launch
                }

                var addSuccessCount = 0
                var addErrorCount = 0
                var removeSuccessCount = 0
                var removeErrorCount = 0
                val errors = mutableListOf<String>()

                // Add to new playlists
                for (playlistId in playlistsToAdd) {
                    try {
                        val response = api.addSongToPlaylist(
                            token = token,
                            request = SongPlaylistRequest(
                                songId = songId,
                                playlistId = playlistId
                            )
                        )

                        when (response.code()) {
                            200, 201 -> addSuccessCount++
                            409 -> {} // Already exists, ignore
                            else -> {
                                addErrorCount++
                                val errorBody = response.errorBody()?.string()
                                val formattedError = formatBackendError(errorBody)
                                errors.add("Add error: $formattedError")
                            }
                        }
                    } catch (e: Exception) {
                        addErrorCount++
                        errors.add("Add network error: ${e.message}")
                    }
                }

                // Remove from playlists
                for (playlistId in playlistsToRemove) {
                    try {
                        // First, get the song-playlist relationship to find the ID
                        Log.d("PlaylistSelection", "Trying to find relationship for songId: $songId, playlistId: $playlistId")
                        val findResponse = api.getSongPlaylistRelation(
                            token = token,
                            songId = songId,
                            playlistId = playlistId
                        )

                        Log.d("PlaylistSelection", "Find response code: ${findResponse.code()}")
                        Log.d("PlaylistSelection", "Find response body: ${findResponse.body()}")

                        if (findResponse.isSuccessful && findResponse.body() != null) {
                            val songPlaylistDTO = findResponse.body()!!
                            Log.d("PlaylistSelection", "Found songPlaylistDTO with ID: ${songPlaylistDTO.id}")
                            
                            if (songPlaylistDTO.id > 0) {
                                // Then delete using the ID
                                val deleteResponse = api.deleteSongPlaylistById(
                                    token = token,
                                    songPlaylistId = songPlaylistDTO.id
                                )

                                Log.d("PlaylistSelection", "Delete response code: ${deleteResponse.code()}")

                                if (deleteResponse.isSuccessful) {
                                    removeSuccessCount++
                                } else {
                                    removeErrorCount++
                                    errors.add("Remove error: ${deleteResponse.errorBody()?.string()}")
                                }
                            } else {
                                removeErrorCount++
                                errors.add("Remove error: Invalid songPlaylistDTO ID (${songPlaylistDTO.id})")
                            }
                        } else {
                            removeErrorCount++
                            val errorMsg = findResponse.errorBody()?.string() ?: "Unknown error"
                            Log.e("PlaylistSelection", "Failed to find song-playlist relationship: $errorMsg")
                            errors.add("Remove error: Failed to find relationship - $errorMsg")
                        }
                    } catch (e: Exception) {
                        removeErrorCount++
                        errors.add("Remove network error: ${e.message}")
                    }
                }

                // Generate result message
                val message = buildString {
                    if (addSuccessCount > 0) {
                        append("Added to $addSuccessCount playlist(s)")
                    }
                    if (removeSuccessCount > 0) {
                        if (isNotEmpty()) append(". ")
                        append("Removed from $removeSuccessCount playlist(s)")
                    }
                    if (addErrorCount > 0 || removeErrorCount > 0) {
                        if (isNotEmpty()) append(". ")
                        append("${addErrorCount + removeErrorCount} operation(s) failed")
                        if (errors.isNotEmpty()) {
                            append(": ${errors.first()}")
                        }
                    }
                }

                onResult(message.ifEmpty { "Operation completed" })

                // Reload to update checkbox states
                loadMyPlaylistsAndCheckSong(songId)

            } catch (e: Exception) {
                onResult("Error updating playlists: ${e.message}")
            }
        }
    }

    private fun formatBackendError(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Unknown error"
        
        return try {
            // Try to extract readable message from backend error
            when {
                errorBody.contains("already exists", ignoreCase = true) -> "Song already in this playlist"
                errorBody.contains("not found", ignoreCase = true) -> "Playlist not found"
                errorBody.contains("permission", ignoreCase = true) -> "No permission to add to this playlist"
                errorBody.contains("invalid", ignoreCase = true) -> "Invalid request"
                else -> errorBody.take(50) // Truncate long error messages
            }
        } catch (e: Exception) {
            "Server error"
        }
    }
}
