package com.example.musicapplicationse114.ui.screen.artists

import androidx.lifecycle.ViewModel
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.FollowArtist
import com.example.musicapplicationse114.model.SongResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ArtistsFollowingViewModel : ViewModel() {
    private val dummySongs = arrayListOf<SongResponse>()
    private val dummyAlbums = arrayListOf<AlbumResponse>()
    private val dummyFollows = arrayListOf<FollowArtist>()

    private val dummyArtists = listOf(
        ArtistResponse(1, "One Republic", "", "", 1000),
        ArtistResponse(2, "Coldplay", "", "", 1200),
        ArtistResponse(3, "Chainsmokers", "", "", 1500),
        ArtistResponse(4, "Linkin Park", "", "", 900),
        ArtistResponse(5, "Sia", "", "", 800),
        ArtistResponse(6, "Ellie Goulding", "", "", 950),
        ArtistResponse(7, "Katy Perry", "", "", 1100),
        ArtistResponse(8, "Maroon 5", "", "", 1300)
    )

    private val _followingArtists = MutableStateFlow(dummyArtists)
    val followingArtists: StateFlow<List<ArtistResponse>> = _followingArtists
}