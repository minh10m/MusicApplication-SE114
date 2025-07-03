package com.example.musicapplicationse114.model

data class SongPlaylistDTO(
    val id: Long,
    val songId: Long,
    val playlistId: Long,
    val addedAt: String?,
    val song: SongResponse?
)
