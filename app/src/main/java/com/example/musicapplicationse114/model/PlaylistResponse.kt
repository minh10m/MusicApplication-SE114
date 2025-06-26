package com.example.musicapplicationse114.model

data class PlaylistResponse(
    val id: Long,
    val userId: Long,
    val name: String,
    val description: String?,
    val thumbnail: String?,
    val createdAt: String, // hoặc LocalDateTime nếu bạn parse
    val isPublic: Boolean,
    val genreIds: List<Long> = emptyList(),
    val songPlaylists: List<SongResponse> = emptyList()
)