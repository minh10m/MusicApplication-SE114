package com.example.musicapplicationse114.model

import java.time.LocalDateTime

data class RecentlyPlayedResponse(val title: String,
                                  val thumbnail: String,
                                  val artistName: String) {
}

data class RecentlyPlayedRequest(val userId : Long,
                                 val songId : Long )

data class RecentlyPlayedPageResponse(val content : List<RecentlyPlayedResponse>)

data class RecentlyPlayed(val id: Long,
                          val user : UserResponse,
                          val song : SongResponse,
                          val addedAt : LocalDateTime)