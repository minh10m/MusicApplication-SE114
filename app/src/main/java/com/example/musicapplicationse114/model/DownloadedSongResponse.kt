package com.example.musicapplicationse114.model

data class DownloadedSongResponse(
    val id: Long,
    val userId: Long,
    val songId: Long,
    val downloadedAt: String
)

data class DownloadedSongPageResponse(
    val content: List<DownloadedSongResponse>
)

