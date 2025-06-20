package com.example.musicapplicationse114.ui.playerController

import androidx.lifecycle.ViewModel
import com.example.musicapplicationse114.model.SongResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerSharedViewModel @Inject constructor(
    val player: GlobalPlayerController
) : ViewModel() {
    fun setSongList(songs: List<SongResponse>, startIndex: Int = 0) {
        player.setSongList(songs, startIndex)
    }
}