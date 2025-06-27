package com.example.musicapplicationse114.model

import org.threeten.bp.LocalDateTime

data class FollowArtistResponse(val id: Long,
                                val userId: Long,
                                val artist: ArtistResponse,
                                val followedAt : String)

data class FollowArtistRequest(val artistId : Long)

data class FollowArtistPageResponse(val content : List<FollowArtistResponse>)