package com.example.musicapplicationse114.ui.screen.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.musicapplicationse114.R
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.RecentlyPlayedResponse
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.repositories.MainLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


data class LibraryUiState(
    val recentlyPlayed: List<SongResponse> = emptyList(),
    val status: LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mainLog : MainLog?,
    private val api : Api?,
    private val tokenManager: TokenManager?
) : ViewModel() {
    val _uiState = MutableStateFlow(LibraryUiState())
    val uiState =  _uiState.asStateFlow()

    fun loadRecentlyPlayed()
    {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            try {
                val token = tokenManager?.getToken()?.takeIf { it.isNotBlank() }
                val userId = tokenManager?.getUserId()?.takeIf { it != -1L }
                if (!token.isNullOrBlank() && api != null && userId != null)
                {
                    val response = api.getRecentlyPlayed(token, userId)
                    if (response.isSuccessful)
                    {
                        _uiState.value = _uiState.value.copy(
                            recentlyPlayed = response.body()?: emptyList(),
                            status = LoadStatus.Success()
                        )
                    }
                    else
                    {
                        val errorBody = response.errorBody()?.string()
                        _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Failed: HTTP ${response.code()} - $errorBody"))
                        Log.e(
                            "LibraryViewModel",
                            "Failed: HTTP ${response.code()} - $errorBody"
                        )
                    }
                }
                else
                {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Missing token or userId or api is null"))
                    Log.e("LibraryViewModel", "Missing token or userId or api is null")
                }

            }
            catch(e:Exception)
            {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Lỗi không xác định"))
                    Log.e("LibraryViewModel", "Failed to load recently played: ${e.message}")
            }
        }
    }
}
