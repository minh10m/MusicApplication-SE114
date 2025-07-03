package com.example.musicapplicationse114.model

data class ProfileDto(
    val username: String,
    val email: String,
    val phone: String,
    val avatar: String,
    val favoriteSongCount: Int,
    val followedArtistCount: Int,
    val playlistCount: Int
)

