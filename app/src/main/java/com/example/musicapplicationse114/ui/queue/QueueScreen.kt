package com.example.musicapplicationse114.ui.queue

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.ui.playerController.PlayerSharedViewModel
import com.example.musicapplicationse114.ui.screen.album.AlbumSongListViewModel
import com.example.musicapplicationse114.ui.screen.artist.ArtistViewModel

@Composable
fun QueueScreen(
    navController: NavController,
    viewModel: QueueViewModel = hiltViewModel(),
    sharedViewModel: PlayerSharedViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
    artistViewModel: ArtistViewModel
) {
    val playerState = sharedViewModel.player.state.collectAsState().value
    val currentSong = playerState.currentSong
    val songList = sharedViewModel.player.getSongList()
    val currentIndex = songList.indexOfFirst { it.id == currentSong?.id }

    LaunchedEffect(Unit) {
        println("QueueScreen: Queue size = ${songList.size}, currentSong = ${currentSong?.title}")
    }
    Column(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp)) {
        // Header với ảnh nền và thông tin bài hát
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            currentSong?.thumbnail?.let { backgroundUrl ->
                Image(
                    painter = rememberAsyncImagePainter(backgroundUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.2f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, top = 36.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Nút Back nằm bên trái
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(100.dp)
                    )
                }
                Spacer(modifier = Modifier.width(40.dp))
                // "Now Playing" và tên bài hát
                Column {
                    Text(
                        "Now Playing:",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        currentSong?.title ?: "",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Row chứa "In Queue"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 18.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "In Queue",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            )
        }

        // LazyColumn cho danh sách bài hát
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 16.dp)
        ) {
            itemsIndexed(songList) { index, song ->
                QueueSongItem(
                    song = song,
                    isCurrent = index == currentIndex,
                    onClick = {
                        sharedViewModel.setSongList(songList, index)
                        sharedViewModel.player.play(song)
                    },
                    artistViewModel
                )
            }
        }
    }
}

@Composable
fun QueueSongItem(
    song: SongResponse,
    isCurrent: Boolean,
    onClick: () -> Unit,
    artistViewModel: ArtistViewModel
) {
    // Box để bao quanh với background có chiều cao động
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp) // Chiều cao cố định của toàn bộ hàng
            .background(
                color = if (isCurrent) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.width(24.dp)) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag Handle",
                    tint = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = rememberAsyncImagePainter(song.thumbnail),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Text("Artist Name", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                Icons.Default.MoreVert,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        if (isCurrent) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(4.dp) // Padding để tạo viền
                    .background(
                        Color.White.copy(alpha = 0.05f), // Màu xám nhạt hơn cho hiệu ứng mở rộng
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}