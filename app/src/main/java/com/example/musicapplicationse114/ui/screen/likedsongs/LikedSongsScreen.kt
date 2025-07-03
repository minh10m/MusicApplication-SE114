package com.example.musicapplicationse114.ui.screen.likedsongs

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.ui.playerController.PlayerSharedViewModel
import com.example.musicapplicationse114.ui.screen.home.HomeViewModel
import kotlinx.coroutines.delay

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun LikedSongsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    viewModel: LikedSongsViewModel = viewModel(),
    mainViewModel: MainViewModel,
    sharedViewModel: PlayerSharedViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.loadFavoriteSong()
    }

    val favoriteSongs = homeViewModel.uiState.value.favoriteSongs // Sử dụng collectAsState để tránh StateFlowValueCalledInComposition
    val globalPlayerController = sharedViewModel.player
    val uiState = viewModel.uiState.collectAsState()
    var showLoading by remember { mutableStateOf(false) }



    val likedSongs : List<SongResponse> = remember(uiState.value.likedSongs) {
        uiState.value.likedSongs.map { it.song }
    }

    val likedSongsSearch : List<SongResponse> = remember(uiState.value.likedSongsSearch) {
        uiState.value.likedSongsSearch.map { it.song }
    }

    if (showLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White,
                strokeWidth = 2.dp)
        }
    }

    LaunchedEffect(showLoading) {
        if (showLoading) {
            delay(1000)
            showLoading = false
        }
    }

    if (uiState.value.status is LoadStatus.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White)
        }
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets
                .safeDrawing
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 25.dp) // Điều chỉnh padding tổng thể
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column() {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIos,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        if (!navController.popBackStack()) {
                                            navController.navigate("library")
                                        }
                                    }
                            )

                            Spacer(modifier = Modifier.width(26.dp))

                            Text(
                                text = "Yêu thích",
                                fontSize = 28.sp, // Tăng kích thước tiêu đề
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                        }
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            text = "${uiState.value.likedSongs.size} bài hát yêu thích",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row {
                    // Search Bar
                    SearchBar(
                        query = uiState.value.query,
                        onQueryChange = {
                            viewModel.updateQuery(it)
                            viewModel.searchAllDebounced(it)
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))
                val displayedSongs = if (uiState.value.query.isNotBlank()) {
                    likedSongsSearch
                } else {
                    likedSongs
                }

                if (uiState.value.status is LoadStatus.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
                        itemsIndexed(displayedSongs) { index, song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sharedViewModel.setSongList(displayedSongs, index)
                                        sharedViewModel.addRecentlyPlayed(song.id)
                                        Log.d(
                                            "LikedSongsScreen",
                                            "Called addRecentlyPlayed for songId: ${song.id}"
                                        )
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
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .width(330.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterVertically) // Đảm bảo Icon căn giữa
        )
        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp), // Tăng nhẹ chiều cao để chứa con trỏ và text
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 14.sp // Đảm bảo line height khớp với fontSize
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center), // Căn giữa BasicTextField trong Box
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Tìm kiếm",
                                color = Color.Black.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                lineHeight = 14.sp, // Đồng bộ với BasicTextField
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
