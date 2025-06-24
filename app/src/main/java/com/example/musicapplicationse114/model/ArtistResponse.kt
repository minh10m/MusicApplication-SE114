package com.example.musicapplicationse114.model

data class ArtistResponse(val id: Long,
                          val name: String,
                          val avatar: String,
                          val description: String,
                          val followerCount: Int)


data class ArtistPageResponse(val content: List<ArtistResponse>)