package com.example.musicapplicationse114.model

import java.time.LocalDateTime

data class RecentlyPlayed(val id : Long, val user : User, val song : SongResponse, val playedAt : LocalDateTime) {
}