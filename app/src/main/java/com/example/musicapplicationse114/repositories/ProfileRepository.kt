package com.example.musicapplicationse114.repositories

import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.model.ProfileDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: Api,
    private val tokenManager: TokenManager
) {
    private val _profile = MutableSharedFlow<ProfileDto?>(replay = 1)
    val profile = _profile.asSharedFlow()

    suspend fun loadProfile(forceRefresh: Boolean = false) {
        if (!forceRefresh && _profile.replayCache.isNotEmpty()) {
            return
        }
        try {
            val token = tokenManager.getToken()
            if (token != null) {
                val response = api.getMyProfile(token)
                if (response.isSuccessful) {
                    _profile.emit(response.body())
                } else {
                    _profile.emit(null)
                }
            } else {
                _profile.emit(null)
            }
        } catch (e: Exception) {
            _profile.emit(null)
        }
    }

    suspend fun updateProfile(
        token: String,
        profilePart: RequestBody,
        avatarPart: MultipartBody.Part?
    ): Boolean {
        try {
            val response = api.updateMyProfile(token, profilePart, avatarPart)
            if (response.isSuccessful) {
                _profile.emit(response.body())
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
}