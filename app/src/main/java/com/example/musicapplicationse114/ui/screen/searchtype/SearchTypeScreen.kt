package com.example.musicapplicationse114.ui.screen.searchtype

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.musicapplicationse114.ui.theme.MusicApplicationSE114Theme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.ui.playerController.PlayerSharedViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTypeScreen(
    navController: NavController,
    viewModel: SearchTypeViewModel,
    mainViewModel: MainViewModel,
    sharedViewModel: PlayerSharedViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val status = uiState.status
    val globalPlayerController = sharedViewModel.player

    LaunchedEffect(Unit) {
        viewModel.loadSong()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 16.dp)
                .padding(top = 40.dp, bottom = 120.dp)
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
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
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

            Spacer(modifier = Modifier.height(3.dp))

            if (uiState.songs1.isNotEmpty() && uiState.query.isBlank()) {
                Text(
                    text = "Gợi ý bài hát",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(uiState.songs1) { index, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sharedViewModel.setSongList(uiState.songs1, index)
                                    sharedViewModel.addRecentlyPlayed(song.id)
                                    Log.d("SearchSongAddIntoPlaylistScreen", "Called addRecentlyPlayed for songId: ${song.id}")
                                    globalPlayerController.play(song)
                                    mainViewModel.setFullScreenPlayer(true)
                                    navController.navigate(Screen.Player.createRoute(song.id))
                                }
                                .padding(vertical = 12.dp), // Tăng padding giữa các mục
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                    imageVector =  Icons.Default.MoreVert,
                                    contentDescription =  "MoreVert",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // Search Results
            if (status is LoadStatus.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
            } else if ((status is LoadStatus.Success || status is LoadStatus.Error) && !uiState.query.isBlank()) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.artists) { artist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("artist/${artist.id}") }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = artist.avatar,
                                contentDescription = artist.name,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = artist.name, color = Color.White, fontSize = 16.sp)
                                Text(text = "Artist", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                    items(uiState.songs) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    globalPlayerController.play(song)
                                    mainViewModel.setFullScreenPlayer(true)
                                    navController.navigate(Screen.Player.createRoute(song.id)) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = song.thumbnail,
                                contentDescription = song.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = song.title, color = Color.White, fontSize = 16.sp)
                                Row()
                                {
                                    Text(text = "Song •", color = Color.Gray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = song.artistName, color = Color.Gray, fontSize = 14.sp)

                                }
                            }
                        }
                    }
                    items(uiState.albums) { album ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("album/${album.id}") }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = album.coverImage,
                                contentDescription = album.name,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = album.name, color = Color.White, fontSize = 16.sp)
                                Row()
                                {
                                    Text(text = "Album •", color = Color.Gray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = album.artistName, color = Color.Gray, fontSize = 14.sp)
                                }

                            }
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
                text = "Tìm kiếm bài hát, nghệ sĩ, album... playlist",
                color = Color.Gray,
                fontSize = 16.sp,
                maxLines = 1
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

