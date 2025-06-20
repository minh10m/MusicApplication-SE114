package com.example.musicapplicationse114.ui.playerController

import com.example.musicapplicationse114.model.SongResponse

data class PlayerState(
    val currentSong: SongResponse? = null,
    val isPlaying: Boolean = false,
    val isLooping: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L
)
