package com.example.musicapplicationse114.ui.screen.playlists

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.model.Playlist
import com.example.musicapplicationse114.ui.theme.MusicApplicationSE114Theme

@Composable
fun PlaylistScreen(
    navController: NavController,
    viewModel: PlaylistViewModel = viewModel(),
    mainViewModel: MainViewModel
) {
    val playlists by viewModel.playlists.collectAsState()
    var showLoading by remember { mutableStateOf(false) }

    if (showLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 80.dp)
        ) {
            Text("Playlists", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            PlaylistSearchBar()

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recents", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(playlists) { playlist ->
                    Column(modifier = Modifier.clickable { /* handle click */ }) {
                        Image(
                            painter = painterResource(id = playlist.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(playlist.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(playlist.subtitle, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistSearchBar() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Search", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewPlaylistScreen() {
    MusicApplicationSE114Theme(darkTheme = true) {
        PlaylistScreen(
            navController = rememberNavController(),
            viewModel = PlaylistViewModel(),
            mainViewModel = MainViewModel()
        )
    }
}
