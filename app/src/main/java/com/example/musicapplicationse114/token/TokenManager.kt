package com.example.musicapplicationse114.auth

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val USER_ID = longPreferencesKey("user_id")
    }

    // Lưu token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = token
        }
    }

    // Lấy token
    suspend fun getToken(): String {
        return context.dataStore.data.map { prefs ->
            prefs[ACCESS_TOKEN] ?: ""
        }.first()
    }

    // Xóa token
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(USER_ID)
        }
    }

    // Lưu userId (Long)
    suspend fun saveUserId(userId: Long) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }

    // Lấy userId (Long)
    suspend fun getUserId(): Long {
        return context.dataStore.data.map { prefs ->
            prefs[USER_ID] ?: -1L
        }.first()
    }

    // Giải mã userId từ access token (dạng JWT)
    fun decodeUserIdFromToken(bearerToken: String): Long? {
        return try {
            val token = bearerToken.removePrefix("Bearer ").trim()
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val json = String(decodedBytes, charset("UTF-8"))
            val payloadObj = JSONObject(json)

            payloadObj.getLong("userId")
        } catch (e: Exception) {
            null
        }
    }
}
