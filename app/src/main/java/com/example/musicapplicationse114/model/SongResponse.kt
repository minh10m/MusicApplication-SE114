package com.example.musicapplicationse114.model

data class SongResponse(
    val id: Long,
    val title: String,
    val duration: Int,
    val audioUrl: String,
    val thumbnail: String,
    val lyrics: String,
    val releaseDate: String, // <-- Đổi từ LocalDate
    val viewCount: Int = 0,
    val artistId: Long,
    val artistName: String,
    val albumId: Long,
    val albumName: String,
    val genreIds: List<Long>
)

data class SongResponseDTO(
    val id: Long,
    val title: String,
    val duration: Int,
    val audioUrl: String,
    val thumbnail: String,
    val lyrics: String,
    val releaseDate: String, // <-- Đổi từ LocalDate
    val viewCount: Int = 0,
    val artistId: Long,
    val artistName: String,
    val albumId: Long,
    val albumName: String,
    val genreIds: List<Long>,
    val isFavorite : Boolean,
    val isDownloaded: Boolean
)

data class SongPageResponse(val content : List<SongResponse>)

data class SongPageResponseDTO(val content : List<SongResponseDTO>)
