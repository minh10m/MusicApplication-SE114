package com.example.musicapplicationse114.ui.screen.artists

import androidx.lifecycle.ViewModel
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.Artist
import com.example.musicapplicationse114.model.FollowArtist
import com.example.musicapplicationse114.model.SongResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ArtistsFollowingViewModel : ViewModel() {
    private val dummySongs = arrayListOf<SongResponse>()
    private val dummyAlbums = arrayListOf<AlbumResponse>()
    private val dummyFollows = arrayListOf<FollowArtist>()

    private val dummyArtists = listOf(
        Artist(1, "One Republic", "", "", 1000, dummyAlbums, dummySongs, dummyFollows),
        Artist(2, "Coldplay", "", "", 1200, dummyAlbums, dummySongs, dummyFollows),
        Artist(3, "Chainsmokers", "", "", 1500, dummyAlbums, dummySongs, dummyFollows),
        Artist(4, "Linkin Park", "", "", 900, dummyAlbums, dummySongs, dummyFollows),
        Artist(5, "Sia", "", "", 800, dummyAlbums, dummySongs, dummyFollows),
        Artist(6, "Ellie Goulding", "", "", 950, dummyAlbums, dummySongs, dummyFollows),
        Artist(7, "Katy Perry", "", "", 1100, dummyAlbums, dummySongs, dummyFollows),
        Artist(8, "Maroon 5", "", "", 1300, dummyAlbums, dummySongs, dummyFollows)
    )

    private val _followingArtists = MutableStateFlow(dummyArtists)
    val followingArtists: StateFlow<List<Artist>> = _followingArtists
}