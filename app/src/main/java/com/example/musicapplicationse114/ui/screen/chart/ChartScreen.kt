package com.example.musicapplicationse114.ui.screen.chart

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.R
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.ui.playerController.PlayerSharedViewModel


@Composable
fun ChartScreen(
    navController: NavController,
    viewModel: ChartViewModel,
    mainViewModel: MainViewModel,
    sharedViewModel: PlayerSharedViewModel
) {
    val state = viewModel.uiState.collectAsState().value
    val globalPlayerController = sharedViewModel.player

    val chartCoverUrl = "https://i.imgur.com/YOUR_IMAGE.jpg" // ảnh nền bảng xếp hạng

    LaunchedEffect(Unit) {
        viewModel.loadSongByTopViewCount()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(bottom = 115.dp)
    ) {
        // Header with background image
        Box(modifier = Modifier.height(130.dp)) {
            Image(
                painter = painterResource(id = R.drawable.chart),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.BottomCenter
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bảng xếp hạng",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chart",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Play button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.White)
                        .clickable {
                            if (state.songs.isNotEmpty()) {
                                sharedViewModel.setSongList(state.songs, 0)
                                sharedViewModel.addRecentlyPlayed(state.songs[0].id)
                                globalPlayerController.play(state.songs[0])
                                mainViewModel.setFullScreenPlayer(true)
                                navController.navigate(Screen.Player.createRoute(state.songs[0].id))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
//                        Icon(
//                            imageVector = Icons.Default.PlayArrow,
//                            contentDescription = "Phát",
//                            tint = Color.Black,
//                            modifier = Modifier.size(24.dp)
//                        )
//                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Phát ngẫu nhiên",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

            }

            // Danh sách bài hát
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.songs) { song ->
                    val index = state.songs.indexOf(song)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sharedViewModel.setSongList(state.songs, index)
                                sharedViewModel.addRecentlyPlayed(song.id)
                                globalPlayerController.play(song)
                                mainViewModel.setFullScreenPlayer(true)
                                navController.navigate(Screen.Player.createRoute(song.id))
                            }
                            .padding(vertical = 12.dp),
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Ảnh bài hát
                        AsyncImage(
                            model = song.thumbnail,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(6.dp))

                        Column(modifier = Modifier.width(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally){
                            // Thứ hạng hoặc vương miện
                            if (index == 0) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_crown),
                                    contentDescription = "Top 1",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(horizontal = 6.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Text(
                                text = "•",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))

                        // Thông tin bài hát
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = song.artistName,
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(36.dp))
                        IconButton(onClick = { /* TODO: More options */ }) {
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
}
