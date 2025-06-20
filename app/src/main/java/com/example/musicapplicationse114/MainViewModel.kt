package com.example.musicapplicationse114

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.repositories.MainLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MainState(
    val error: String = ""
)

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {
    val _uiState = MutableStateFlow(MainState())
    val uiState = _uiState.asStateFlow()

    val isFullScreenPlayer = mutableStateOf(false)



    fun setError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    fun setFullScreenPlayer(value: Boolean) {
        isFullScreenPlayer.value = value
    }
}