package com.example.musicapplicationse114.model

import androidx.compose.ui.semantics.Role

data class UserResponse(val id: Long, val role: Role, val username: String, val email: String,
                        val phone : String, val avatar : String)