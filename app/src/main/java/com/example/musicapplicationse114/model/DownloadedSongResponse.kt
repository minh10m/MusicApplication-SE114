package com.example.musicapplicationse114.model

data class DownloadedSongResponse(
    val id: Long,
    val song: SongResponse,
    val downloadedAt: String
)

data class DownloadedSongPageResponse(
    val content: List<DownloadedSongResponse>
)

data class DownloadSongDetail(val id : Long,
                              val title: String,
                              val duration: Int,
                              val thumbnail : String,
                              val releaseDate : String,
                              val artistName: String,
                              val albumName: String)
