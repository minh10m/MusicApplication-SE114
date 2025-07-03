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
    val status : LoadStatus = LoadStatus.Init(),
    val successMessage : String = "",
    val error : String = ""
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

    fun updateSuccessMessage(message : String) {
        _uiState.value = uiState.value.copy(successMessage = message)
    }

    fun updateError(error : String) {
        _uiState.value = uiState.value.copy(error = error)
    }
    fun updateDescription(description : String) {
        _uiState.value = uiState.value.copy(description = description)
    }

    fun updateIsPublic(isPublic : Boolean) {
        _uiState.value = uiState.value.copy(isPublic = isPublic)
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(status = LoadStatus.Init())
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
                    if(response.isSuccessful)
                    {
                        Log.d("CreatePlaylistViewModel", "Playlist created successfully")
                        updateSuccessMessage("Tạo playlist thành công")
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Success())

                    }
                    else{
                        Log.e("CreatePlaylistViewModel", "Failed to create playlist: ${response.code()}")
                        updateError("Tạo playlist thất bại")
                    }
                }
                else {
                    _uiState.value =
                        _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                    Log.e("CreatePlaylistViewModel", "Token hoặc API null")
                    updateError("Tạo playlist thất bại")
                }
                }
            catch (e : Exception) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                    Log.e("CreatePlaylistViewModel", "Failed to create playlist: ${e.message}")
                    updateError("Tạo playlist thất bại")
            }
        }
    }

}