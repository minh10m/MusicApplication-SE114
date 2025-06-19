package com.example.musicapplicationse114.ui.screen.download

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.ui.theme.MusicApplicationSE114Theme
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DownloadSongsScreen(
    navController: NavController,
    viewModel: DownloadSongsViewModel = viewModel(),
    mainViewModel: MainViewModel
) {
    val songs by viewModel.downloadedSongs.collectAsState()
    var showLoading by remember { mutableStateOf(false) }

    if (showLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    LaunchedEffect(showLoading) {
        if (showLoading) {
            delay(1000)
            showLoading = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
//        bottomBar = { NavigationBar(navController = navController) { showLoading = true } }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 80.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            if (!navController.popBackStack()) {
                                navController.navigate("library")
                            }
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Downloaded Songs",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${songs.size} downloaded songs",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SearchBar()

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(songs) { song ->
                    SongItem(song)
                }
            }
        }
    }
}

@Composable
fun SearchBar() {
    var text by remember { mutableStateOf("") }
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Search", color = Color.White.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun SongItem(song: SongResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(Color.Gray, RoundedCornerShape(5.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            //Text(song.artist.name, color = Color.Gray, fontSize = 13.sp)
        }
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.LightGray)
        Spacer(modifier = Modifier.width(10.dp))
        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.LightGray)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDownloadSongsScreen() {
    MusicApplicationSE114Theme(darkTheme = true) {
        DownloadSongsScreen(
            navController = rememberNavController(),
            viewModel = DownloadSongsViewModel(),
            mainViewModel = MainViewModel()
        )
    }
}
