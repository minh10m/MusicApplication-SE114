package com.example.musicapplicationse114.model

data class FavoriteSongResponse(
    val id: Long,
    val userId: Long,
    val songId: Long,
    val addedAt: String
)

data class FavoriteSongPageResponse(val content : List<FavoriteSongResponse>)

data class AddFavoriteSongRequest(
    val userId: Long,
    val songId: Long
)
