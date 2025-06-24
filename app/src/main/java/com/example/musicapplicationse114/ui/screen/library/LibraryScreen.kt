package com.example.musicapplicationse114.ui.screen.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.ui.screen.search.SearchBottomNavigationBar
import com.example.musicapplicationse114.ui.theme.MusicApplicationSE114Theme
import kotlinx.coroutines.delay

@Composable
fun LibraryScreen(navController: NavController, viewModel: LibraryViewModel = viewModel(), mainViewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showLoading by remember { mutableStateOf(false) }

    // Khi showLoading = true, hiển thị loading indicator
    if (showLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // Tắt loading sau 1 giây
    LaunchedEffect(showLoading) {
        if (showLoading) {
            delay(1000)
            showLoading = false
        }
    }

    Scaffold() {innerPadding->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 129.dp)
        ) {
            Text("Your Library", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            LibraryGrid(state)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recently Played", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("See more", color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(state.recentItems) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = song.imageRes),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, color = Color.White, fontSize = 14.sp)
                            Text(song.subtitle, color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                }
            }
        }

//        SearchBottomNavigationBar(
//            modifier = Modifier.align(Alignment.BottomCenter),
//            onHomeClick = {},
//            onSearchClick = {},
//            onLibraryClick = {} // current
//        )
    }
}

@Composable
fun LibraryGrid(state: LibraryUiState) {
    val items = listOf(
        LibraryTile("Liked Songs", "${state.likedCount} songs", Icons.Default.Favorite),
        LibraryTile("Downloads", "${state.downloadedCount} songs", Icons.Default.Download),
        LibraryTile("Playlists", "${state.playlistCount} playlists", Icons.Default.List),
        LibraryTile("Artists", "${state.artistCount} artists", Icons.Default.Person)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (i in items.chunked(2)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (item in i) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.DarkGray, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(item.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(item.subtitle, color = Color.Gray, fontSize = 12.sp)
                    }
                }
                if (i.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

data class LibraryTile(val title: String, val subtitle: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LibraryScreenPreview() {
    MusicApplicationSE114Theme(darkTheme = true) {
        val navController = rememberNavController()
        LibraryScreen(navController = navController, viewModel = LibraryViewModel(null, null), mainViewModel = MainViewModel())
    }
}
