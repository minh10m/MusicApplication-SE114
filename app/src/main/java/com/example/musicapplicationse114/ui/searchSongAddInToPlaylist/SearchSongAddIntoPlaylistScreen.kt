package com.example.musicapplicationse114.ui.searchSongAddInToPlaylist

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.ui.playerController.PlayerSharedViewModel
import com.example.musicapplicationse114.ui.screen.home.HomeViewModel
import com.example.musicapplicationse114.ui.screen.playListSongs.PlayListSongsViewModel

@Composable
fun SearchSongAddIntoPlaylistScreen(
    navController: NavController,
    viewModel: SearchSongAddIntoPlaylistViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
    sharedViewModel: PlayerSharedViewModel,
    homeViewModel: HomeViewModel,
    playListSongsViewModel: PlayListSongsViewModel
    ) {
    val uiState by viewModel.uiState.collectAsState()
    val status = uiState.status
    val globalPlayerController = sharedViewModel.player
    val homeUiState by homeViewModel.uiState.collectAsState()
    val playListSongsUiState by playListSongsViewModel.uiState.collectAsState()

    val previousBackStackEntry = navController.previousBackStackEntry
    val playlistId = previousBackStackEntry?.savedStateHandle?.get<Long>("playlistId") ?: 0L
    val addedSongIds = previousBackStackEntry?.savedStateHandle?.get<List<Long>>("addedSongIds")?.toSet() ?: emptySet()

    LaunchedEffect(Unit) {
        viewModel.setPlaylistInfo(playlistId, addedSongIds)
        viewModel.loadSong()
    }


    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 16.dp)
                .padding(top = 40.dp, bottom = 110.dp)
        ) {
            // Back + Search bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            navController.previousBackStackEntry?.savedStateHandle?.set("reload", true)
                            navController.popBackStack()

                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                SearchBar(
                    query = uiState.query,
                    onQueryChange = {
                        viewModel.updateQuery(it)
                        viewModel.searchAllDebounced(it)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            val displayedSongs = if (uiState.query.isNotBlank()) {
                uiState.songsSearch
            } else {
                uiState.songs
            }

            if(displayedSongs == uiState.songs)
            {
                Text(
                    text = "Gợi ý bài hát",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

//            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(displayedSongs) { index, song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sharedViewModel.setSongList(displayedSongs, index)
                                sharedViewModel.addRecentlyPlayed(song.id)
                                Log.d("SearchSongAddIntoPlaylistScreen", "Called addRecentlyPlayed for songId: ${song.id}")
                                globalPlayerController.play(song)
                                mainViewModel.setFullScreenPlayer(true)
                                navController.navigate(Screen.Player.createRoute(song.id))
                            }
                            .padding(vertical = 12.dp), // Tăng padding giữa các mục
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isAdded = uiState.addedSongIds.contains(song.id)
                        AsyncImage(
                            model = song.thumbnail,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(50.dp) // Tăng kích thước hình ảnh
                                .clip(RoundedCornerShape(8.dp)), // Bo góc lớn hơn
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artistName,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = if (isAdded) "Added" else "Add",
                                tint = if (isAdded) Color.Green else Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        if (!isAdded) {
                                            viewModel.addSongToPlaylist(song.id, uiState.playlistId)
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search songs",
                color = Color.Gray,
                fontSize = 16.sp
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .background(Color.Transparent, RoundedCornerShape(12.dp)) // Thêm background để đẹp hơn
    )
}