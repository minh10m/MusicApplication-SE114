package com.example.musicapplicationse114.ui.screen.artist

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.musicapplicationse114.ui.screen.album.AlbumSongListViewModel

@Composable
fun ArtistSongListScreen(
    navController: NavController,
    artistId: Long,
    viewModel: ArtistViewModel = hiltViewModel(),
    mainViewModel : MainViewModel,
    sharedViewModel: PlayerSharedViewModel = hiltViewModel()
) {
    Log.d("ArtistSongListScreen", "PlayerSharedViewModel instance: ${sharedViewModel.hashCode()}")
    val state = viewModel.uiState.collectAsState().value
    val globalPlayerController = sharedViewModel.player

    LaunchedEffect(artistId) {
        viewModel.loadArtistById(artistId)
        viewModel.loadSongByArtistId(artistId)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(bottom = 129.dp)) {
        // Fixed album cover with overlay
        Box(modifier = Modifier.height(300.dp)) {
            AsyncImage(
                model = state.artist?.body()?.avatar,
                contentDescription = state.artist?.body()?.name,
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
                        text = state.artist?.body()?.name ?: "",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Artist",
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
                text = "${state.artist?.body()?.followerCount} likes • ${state.songArtist.size} songs",
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
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Follow",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                IconButton(
                    onClick = {
                        if (state.songArtist.isNotEmpty()) {
                            sharedViewModel.setSongList(state.songArtist, 0)
                            sharedViewModel.addRecentlyPlayed(state.songArtist[0].id)
                            Log.d("ArtistSongListScreen", "Called addRecentlyPlayed for songId: ${state.songArtist[0].id}")
                            mainViewModel.setFullScreenPlayer(true)
                            globalPlayerController.play(state.songArtist[0])
                            navController.navigate(Screen.Player.createRoute(state.songArtist[0].id))
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
            items(state.songArtist) { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            sharedViewModel.setSongList(state.songArtist, state.songArtist.indexOf(song))
                            sharedViewModel.addRecentlyPlayed(song.id)
                            Log.d("ArtistSongListScreen", "Called addRecentlyPlayed for songId: ${song.id}")
                            mainViewModel.setFullScreenPlayer(true)
                            globalPlayerController.play(song)
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