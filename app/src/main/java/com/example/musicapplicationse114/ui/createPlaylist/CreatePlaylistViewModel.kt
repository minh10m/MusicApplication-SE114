package com.example.musicapplicationse114.ui.createPlaylist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.PlaylistRequest
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePlaylistUiState(
    val name : String = "",
    val description : String = "",
    val isPublic : Boolean = false,
    val status : LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    val api : Api?,
    val tokenManager: TokenManager?
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState : StateFlow<CreatePlaylistUiState> = _uiState

    fun updateName(name : String) {
        _uiState.value = uiState.value.copy(name = name)
    }

    fun updateDescription(description : String) {
        _uiState.value = uiState.value.copy(description = description)
    }

    fun updateIsPublic(isPublic : Boolean) {
        _uiState.value = uiState.value.copy(isPublic = isPublic)
    }

    fun createPlaylist() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if(!token.isNullOrBlank() && api != null) {
                    val response = api.createPlaylist(token, PlaylistRequest(
                        uiState.value.name,
                        uiState.value.description,
                        uiState.value.isPublic))
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Success())
                    if(response.isSuccessful)
                    {
                        Log.d("CreatePlaylistViewModel", "Playlist created successfully")
                    }
                    else{
                        Log.e("CreatePlaylistViewModel", "Failed to create playlist: ${response.code()}")
                    }
                }
                else {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                    Log.e("CreatePlaylistViewModel", "Token hoặc API null")
                    }
                }
            catch (e : Exception) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                    Log.e("CreatePlaylistViewModel", "Failed to create playlist: ${e.message}")
            }
        }
    }

}