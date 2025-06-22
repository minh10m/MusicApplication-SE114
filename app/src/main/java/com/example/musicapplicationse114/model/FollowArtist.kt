package com.example.musicapplicationse114.model

import org.threeten.bp.LocalDateTime

data class FollowArtist(val id: Long, val userId: Long,
                        val artist: ArtistResponse, val followedAt : LocalDateTime = LocalDateTime.now()) {
}