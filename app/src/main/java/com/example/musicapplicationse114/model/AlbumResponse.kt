package com.example.musicapplicationse114.model

data class AlbumResponse(val id : Long,
                         val name : String,
                         val releaseDate: String,
                         val coverImage : String,
                         val description: String,
                         val artistId : Long)

data class AlbumPageResponse(val content : List<AlbumResponse>)

