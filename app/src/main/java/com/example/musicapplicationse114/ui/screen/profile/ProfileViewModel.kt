package com.example.musicapplicationse114.ui.screen.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.model.ProfileDto
import com.example.musicapplicationse114.model.UserUpdateDTO
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.ui.notification.LoadState
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val api: Api,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _profile = MutableStateFlow<ProfileDto?>(null)
    val profile = _profile.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState = _loadState.asStateFlow()

    private val _logoutState = MutableStateFlow<LoadState>(LoadState.Idle)
    val logoutState = _logoutState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    val response = api.getMyProfile(token)
                    if (response.isSuccessful) {
                        _profile.value = response.body()
                        _loadState.value = LoadState.Success
                    } else {
                        _loadState.value = LoadState.Error("Failed to load profile")
                    }
                } else {
                    _loadState.value = LoadState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                _loadState.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LoadState.Loading
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    val response = api.logout(token)
                    if (response.isSuccessful) {
                        tokenManager.clearToken()
                        _logoutState.value = LoadState.Success
                    } else {
                        _logoutState.value = LoadState.Error("Logout failed")
                    }
                } else {
                    _logoutState.value = LoadState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                _logoutState.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateProfile(
        username: String,
        email: String,
        phone: String,
        avatarUri: Uri?=null,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch

                // Convert UserUpdateDTO to JSON string and create RequestBody
                val userUpdateDTO = UserUpdateDTO(username, email, phone)
                val gson = Gson()
                val json = gson.toJson(userUpdateDTO)
                val profilePart = json.toRequestBody("application/json".toMediaType())

                // Convert avatarUri to MultipartBody.Part
                val avatarPart = avatarUri?.let {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                    val bytes = inputStream?.readBytes() ?: byteArrayOf()
                    val requestBody = bytes.toRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("avatarFile", "avatar.jpg", requestBody)
                }

                val response = api.updateMyProfile(token, profilePart, avatarPart)
                if (response.isSuccessful) {
                    _profile.value = response.body()
                    onSuccess()
                } else {
                    // Log error or handle it properly
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
