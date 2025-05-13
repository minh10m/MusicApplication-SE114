package com.example.musicapplicationse114.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.repositories.MainLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val successMessage : String = "",
    var isShowPassword: Boolean = false,
    val status: LoadStatus = LoadStatus.Init()
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val mainLog: MainLog?,
    private val api: Api?
): ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateSuccessMessage(successMessage: String){
        _uiState.value = _uiState.value.copy(successMessage = successMessage)
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(status = LoadStatus.Init())
    }

    fun changeIsShowPassword() {
        _uiState.value = _uiState.value.copy(isShowPassword = !_uiState.value.isShowPassword)
    }

    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = LoadStatus.Loading())
            try {
                val result = api?.login(
                    _uiState.value.username,
                    _uiState.value.password
                )
                if (result == true) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Success(),
                        successMessage = "Login successful"
                    )
                }
            } catch (ex: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(ex.message ?: "Unknown error")
                )
            }
        }
    }

    fun getUserName(): String {
        return _uiState.value.username
    }
}