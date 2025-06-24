package com.example.musicapplicationse114.ui.screen.likedsongs

import androidx.lifecycle.ViewModel
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.GenreResponse
import com.example.musicapplicationse114.model.SongResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class LikedSongsViewModel : ViewModel() {
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
        artistId = 46546,
    )

    private val dummyGenre = GenreResponse(
        id = 1,
        name = "EDM",
        description = "",
    )

    private val dummySongs = listOf(
        SongResponse(1, "Inside Out", 220, "", "", "", "2020-01-01", 0, 435345, 4567465, emptyList()),
        SongResponse(2, "Young", 210, "", "", "", "2020-01-01", 0, 354346, 456456, emptyList()),
        SongResponse(3, "Beach House", 250, "", "", "", "2020-01-01", 0, 345345, 456456, emptyList()),
        SongResponse(4, "Kills You Slowly", 200, "", "", "", "2020-01-01", 0, 34534, 456456, emptyList())
    )

    private val _likedSongs = MutableStateFlow(dummySongs)
    open val likedSongs: StateFlow<List<SongResponse>> = _likedSongs
}
