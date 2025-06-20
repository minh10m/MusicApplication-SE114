package com.example.musicapplicationse114.model

data class Artist(val id: Long, val name: String,
                  val avatar: String, val description: String,
                  val followersCount: Int = 0, val albums: ArrayList<AlbumResponse>,
                  val songs : ArrayList<SongResponse>, val followers: ArrayList<FollowArtist>){
}
