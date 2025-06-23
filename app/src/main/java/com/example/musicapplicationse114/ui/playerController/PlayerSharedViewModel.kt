package com.example.musicapplicationse114.ui.playerController

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.musicapplicationse114.model.SongResponse
import dagger.hilt.android.lifecycle.HiltViewModel
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

//    fun play(song: SongResponse) {
//        val index = _queue.indexOfFirst { it.id == song.id }
//        if (index != -1) {
//            _currentIndex.value = index
//            player.play(song)
//        }
//    }
//
//    fun next() {
//        if (_currentIndex.value + 1 < _queue.size) {
//            _currentIndex.value++
//            player.play(_queue[_currentIndex.value])
//        }
//    }
//
//    fun previous() {
//        if (_currentIndex.value > 0) {
//            _currentIndex.value--
//            player.play(_queue[_currentIndex.value])
//        }
//    }

    fun getUpNext(): SongResponse? {
        return _queue.getOrNull(_currentIndex.value + 1)
    }
}