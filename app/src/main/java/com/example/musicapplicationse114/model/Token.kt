package com.example.musicapplicationse114.model

data class Token(val id: Integer, val accessToken: String, val refreshToken: String,
                 val loggedOut: Boolean, val user: UserResponse) {
}