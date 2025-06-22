package com.example.musicapplicationse114.ui.screen.download

import androidx.lifecycle.ViewModel
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.Genre
import com.example.musicapplicationse114.model.SongResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DownloadSongsViewModel : ViewModel() {
    private val dummyArtist = ArtistResponse(
        id = 1,
        name = "The Chainsmokers",
        avatar = "",
        description = "",
        followerCount = 0
    )

    private val dummyAlbum = AlbumResponse(
        id = 1,
        name = "Test Album",
        releaseDate = "2020-01-01",
        coverImage = "",
        description = "",
        artistId = 454534,
    )

    private val dummyGenre = Genre(
        id = 1,
        name = "EDM",
        description = "",
        Song = arrayListOf()
    )

    private val dummySongs = listOf(
        SongResponse(1, "Inside Out", 220, "", "", "", "2020-01-01", 0, 23424, 234234, emptyList()),
        SongResponse(2, "Young", 210, "", "", "", "2020-01-01", 0, 243423, 5435, emptyList()),
        SongResponse(3, "Beach House", 250, "", "", "", "2020-01-01", 0, 234234, 3454, emptyList()),
        SongResponse(4, "Kills You Slowly", 200, "", "", "", "2020-01-01", 0, 243234, 345435, emptyList())
    )

    private val _downloadedSongs = MutableStateFlow(dummySongs)
    val downloadedSongs: StateFlow<List<SongResponse>> = _downloadedSongs
}
