package com.example.musicapplicationse114.ui.screen.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.repositories.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChartUiState(
    val songs : List<SongResponse> = emptyList(),
    val status : LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val api: Api?,
    private val tokenManager: TokenManager?
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState = _uiState

    fun loadSongByTopViewCount() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
                val token = tokenManager?.getToken()
                if(!token.isNullOrBlank() && api != null)
                {
                    val songs = api.getTopSongByViewCount(token)
                    Log.d("ChartViewModel", "Songs loaded successfully")
                    _uiState.value = songs.body()?.content?.let {
                        _uiState.value.copy(
                            songs = it,
                            status = LoadStatus.Success()
                        )
                    }!!
                }
                else
                {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error("Token hoặc API null"))
                }
            }
            catch(e : Exception)
            {
                _uiState.value = _uiState.value.copy(status = LoadStatus.Error(e.message ?: "Unknown error"))
                Log.e("ChartViewModel", "Failed to load song: ${e.message}")
            }

        }
    }
}