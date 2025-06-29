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
    val songPlaylists: List<SongPlaylist> = emptyList()
)

data class PlaylistResquest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val genreIds: List<Long>? = null
)

data class SongPlaylist(
    val id: Long,
    val songId: Long,
    val playlistId: Long,
    val addedAt: String,
    val song: SongResponse
)

data class PlaylistPageResponse<T>(
    val content : List<T>
)