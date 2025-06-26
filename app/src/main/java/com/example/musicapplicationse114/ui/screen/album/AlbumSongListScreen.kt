package com.example.musicapplicationse114.ui.screen.album

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.musicapplicationse114.ui.playerController.PlayerSharedViewModel

@Composable
fun AlbumSongListScreen(
    navController: NavController,
    albumId: Long,
    viewModel: AlbumSongListViewModel = hiltViewModel(),
    sharedViewModel: PlayerSharedViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
) {
    Log.d("AlbumSongListScreen", "PlayerSharedViewModel instance: ${sharedViewModel.hashCode()}")
    val state = viewModel.uiState.collectAsState().value
    val globalPlayerController = sharedViewModel.player

    LaunchedEffect(albumId) {
        viewModel.loadAlbumById(albumId)
        viewModel.loadSongByAlbumId(albumId)
//        sharedViewModel.setSongList(state.songAlbums, 0)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(bottom = 129.dp)) {
        // Fixed album cover with overlay
        Box(modifier = Modifier.height(300.dp)) {
            AsyncImage(
                model = state.album?.coverImage,
                contentDescription = state.album?.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.TopStart)
                        .padding(top = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.album?.name ?: "",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Album",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Info and action row below cover
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "1.2K likes • ${state.songAlbums.size} songs",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                IconButton(
                    onClick = {
                        if (state.songAlbums.isNotEmpty()) {
                            sharedViewModel.setSongList(state.songAlbums, 0)
                            sharedViewModel.addRecentlyPlayed(state.songAlbums[0].id)
                            Log.d("AlbumSongListScreen", "Called addRecentlyPlayed for songId: ${state.songAlbums[0].id}")
                            globalPlayerController.play(state.songAlbums[0])
                            mainViewModel.setFullScreenPlayer(true)
                            navController.navigate(Screen.Player.createRoute(state.songAlbums[0].id))
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play All",
                        tint = Color.Black,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }

        // Song list
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.songAlbums) { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            sharedViewModel.setSongList(state.songAlbums, state.songAlbums.indexOf(song))
                            sharedViewModel.addRecentlyPlayed(song.id)
                            Log.d("AlbumSongListScreen", "Called addRecentlyPlayed for songId: ${song.id}")
                            globalPlayerController.play(song)
                            mainViewModel.setFullScreenPlayer(true)
                            navController.navigate(Screen.Player.createRoute(song.id))
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.thumbnail,
                        contentDescription = song.title,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = state.artist?.body()?.name ?: "Unknown Artist",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = { /* handle more */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
