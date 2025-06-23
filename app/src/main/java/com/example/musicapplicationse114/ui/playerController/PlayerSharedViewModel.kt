package com.example.musicapplicationse114.ui.playerController

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.model.SongResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerSharedViewModel @Inject constructor(
    val player: GlobalPlayerController
) : ViewModel() {
    private val _queue = mutableStateListOf<SongResponse>()
    val queue: List<SongResponse> get() = _queue

    private val _currentIndex = mutableStateOf(0)
    val currentIndex: State<Int> get() = _currentIndex

    val currentSong: SongResponse?
        get() = _queue.getOrNull(_currentIndex.value)

    fun setSongList(songs: List<SongResponse>, startIndex: Int = 0) {
        _queue.clear()
        _queue.addAll(songs)
        _currentIndex.value = startIndex
        player.setSongList(songs, startIndex)
    }

    init {
        // Lắng nghe trạng thái của GlobalPlayerController
        viewModelScope.launch {
            player.state.collectLatest { state ->
                state.currentSong?.let { currentSong ->
                    val index = player.getSongList().indexOfFirst { it.id == currentSong.id }
                    if (index != -1 && index != _currentIndex.value) {
                        _currentIndex.value = index
                    }
                }
            }
        }
    }

    fun getUpNext(): SongResponse? {
        return _queue.getOrNull(_currentIndex.value + 1)
    }
}