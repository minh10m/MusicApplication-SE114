package com.example.musicapplicationse114.ui.screen.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.R
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.common.enum.TimeOfDay
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.ui.playerController.PlayerSharedViewModel
import com.example.musicapplicationse114.ui.screen.player.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

data class TabItem(val text: String, val screen: @Composable () -> Unit)

@Composable
fun Home(
    viewModel: HomeViewModel,
    navController: NavController,
    mainViewModel: MainViewModel,
    sharedViewModel: PlayerSharedViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val globalPlayerController = sharedViewModel.player

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(bottom = 129.dp, start = 2.dp) // Tăng padding để tránh che bởi MiniPlayer và NavigationBar
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Albums nổi bật",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp)
            ) {
                items(items = state.albums) { album ->
                    AlbumItem(album)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Gợi ý bài hát",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp)
            ) {
                items(items = state.songs) { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            Log.d("HomeScreen", "Playing song: ${song.title}, id: ${song.id}")
                            sharedViewModel.setSongList(state.songs, state.songs.indexOf(song))
                            globalPlayerController.play(song)
                            mainViewModel.setFullScreenPlayer(true)
                            navController.navigate(Screen.Player.createRoute(song.id))
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "ID bài hát yêu thích",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                items(state.favoriteSongs.toList()) { id ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color.DarkGray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "ID: $id", color = Color.White)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "ID bài hát đã tải",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                items(state.downloadSongs.toList()) { id ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color.DarkGray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "ID: $id", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumItem(album: AlbumResponse) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(170.dp)
    ) {
        Box(
            modifier = Modifier
                .height(170.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = album.coverImage,
                contentDescription = album.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = album.name,
            color = Color.LightGray,
            fontSize = 16.sp,
            maxLines = 1
        )
    }
}

@Composable
fun SongItem(song: SongResponse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(320.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = song.thumbnail,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = song.title,
            color = Color.LightGray,
            fontSize = 16.sp,
            maxLines = 1
        )
    }
}

fun formatDate(input: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(input, inputFormatter)
        outputFormatter.format(date)
    } catch (e: Exception) {
        "N/A"
    }
}

@Composable
fun Relax1() {
    Column {
        Image(
            painter = painterResource(R.drawable.relax),
            contentDescription = null,
            modifier = Modifier
                .size(width = 1000.dp, height = 1000.dp)
                .offset(x = (-10).dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .offset(y = 575.dp)
                .background(Color.Black.copy(alpha = 0.8f))
        )
    }
}

@Composable
fun Workout() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Workout Content", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
fun Travel() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Travel Content", color = Color.White, fontSize = 20.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    mainViewModel: MainViewModel,
    username: String,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    sharedViewModel: PlayerSharedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var tabIndex by rememberSaveable { mutableStateOf(0) }

    val tabs = listOf(
        TabItem("For you") { Home(viewModel, navController, mainViewModel, sharedViewModel) },
        TabItem("Relax") { Relax1() },
        TabItem("Workout") { Workout() },
        TabItem("Travel") { Travel() }
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var showLoading by remember { mutableStateOf(false) }

    if (showLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    LaunchedEffect(showLoading) {
        if (showLoading) {
            delay(1000)
            showLoading = false
        }
    }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(username) {
        viewModel.setTimeOfDay()
        viewModel.updateUserName(username)
        viewModel.loadAlbum()
        viewModel.loadSong()
        viewModel.loadDownloadedSong()
        viewModel.loadFavoriteSong()
        Log.d("HomeScreen", "Username: ${viewModel.getUserName()}, TimeOfDay: ${viewModel.getTimeOfDay()}")
    }

    val greeting = when (state.timeOfDay) {
        TimeOfDay.MORNING -> "Good Morning"
        TimeOfDay.AFTERNOON -> "Good Afternoon"
        TimeOfDay.EVENING -> "Good Evening"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(start = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.hello),
                        contentDescription = null,
                        modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Hi ${state.username},",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Log.d("HomeScreen", "Displayed username: ${state.username}")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    greeting,
                    fontSize = 21.sp,
                    color = Color.White,
                    fontWeight = FontWeight.W700
                )
                Log.d("HomeScreen", "Displayed greeting: $greeting")
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.bell),
                contentDescription = "Notifications",
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        Log.d("HomeScreen", "Notification bell clicked")
                    }
            )
            Spacer(modifier = Modifier.width(15.dp))
            Image(
                painter = painterResource(id = R.drawable.logan),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        Log.d("HomeScreen", "Profile image clicked")
                    }
            )
        }
        Spacer(modifier = Modifier.height(15.dp))

        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color.Black,
            contentColor = Color.LightGray,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                    color = Color.DarkGray,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, tabItem ->
                Tab(
                    selected = tabIndex == index,
                    onClick = {
                        tabIndex = index
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(tabItem.text, fontSize = 15.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            userScrollEnabled = false
        ) { page ->
            tabs[page].screen()
        }
    }
}