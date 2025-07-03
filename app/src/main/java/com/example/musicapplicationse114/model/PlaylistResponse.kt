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

data class PlaylistRequest(
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

data class SongPlaylistRequest(
    val songId : Long,
    val playlistId : Long
)

data class SongPlaylistResponse(
    val id: Long,
    val songId: Long,
    val playlistId: Long,
    val addedAt: String,
    val song: SongResponse? = null
)

data class PlaylistPageResponse<T>(
    val content : List<T>
)