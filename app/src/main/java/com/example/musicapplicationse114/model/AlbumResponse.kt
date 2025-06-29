package com.example.musicapplicationse114.model

data class AlbumResponse(val id : Long,
                         val name : String,
                         val releaseDate: String,
                         val coverImage : String,
                         val description: String,
                         val artistId : Long,
                         val artistName : String)

data class AlbumPageResponse(val content : List<AlbumResponse>)

data class GlobalSearchResultDTO(val songs: List<SongResponse> = emptyList(),
                                 val playlists: List<PlaylistResponse> = emptyList(),
                                 val artists: List<ArtistResponse> = emptyList(),
                                 val albums: List<AlbumResponse> = emptyList(),
                                 val totalResults : Long = 0)

