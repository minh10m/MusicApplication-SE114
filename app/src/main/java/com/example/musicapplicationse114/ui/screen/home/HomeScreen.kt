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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.GenreResponse
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
                    AlbumItem(
                        album = album,
                        onClick = {
                            navController.navigate(Screen.Album.createRoute(album.id))
                        }
                    )
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
                            sharedViewModel.addRecentlyPlayed(song.id)
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
                "Nghệ sĩ top trending",
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
                items(items = state.artists) { artist ->
                    ArtistItem(artist = artist,
                        onClick = {navController.navigate(Screen.Artist.createRoute(artist.id))})
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
                        Text(text = "ID: ${id.id}", color = Color.White)
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
                        Text(text = "ID: ${id.id}", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumItem(album: AlbumResponse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(170.dp)
            .clickable { onClick() }
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
fun ArtistItem(artist: ArtistResponse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(170.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = artist.avatar,
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = artist.name,
            color = Color.LightGray,
            fontSize = 16.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun SongItem(song: SongResponse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(170.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .height(170.dp)
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
fun Relax1(
    viewModel: HomeViewModel,
    navController: NavController,
    mainViewModel: MainViewModel,
    sharedViewModel: PlayerSharedViewModel
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (state.genres.isEmpty()) {
            viewModel.loadGenre()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(bottom = 55.dp, top = 0.dp)
    ) {
        Text(
            text = "Thể loại nhạc bạn có thể thích",
            color = Color.LightGray,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 8.dp, top = 16.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.genres) { genre ->
                GenreItemWithSongs(
                    genre = genre,
                    viewModel = viewModel,
                    navController = navController,
                    mainViewModel = mainViewModel,
                    sharedViewModel = sharedViewModel
                )
            }
        }
    }
}

@Composable
fun GenreItemWithSongs(
    genre: GenreResponse,
    viewModel: HomeViewModel,
    navController: NavController,
    mainViewModel: MainViewModel,
    sharedViewModel: PlayerSharedViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val songs = state.songsByGenre[genre.id] ?: emptyList() // Lấy danh sách bài hát theo genreId
    val genreThumbnail = songs.getOrNull(2)?.thumbnail

    LaunchedEffect(genre.id) {
        if (songs.isEmpty()) {
            viewModel.loadSongByGenre(genre.id)
        }
    }

    Column(
        modifier = Modifier
            .width(300.dp)
            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )
            {
                AsyncImage(
                    model = genreThumbnail,
                    contentDescription = genre.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = genre.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${songs.size} songs",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 2.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Row {
                IconButton(onClick = { /* Like action */ }) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* Menu action */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(120.dp))
            IconButton(
                onClick = {
                    if (songs.isNotEmpty()) {
                        sharedViewModel.setSongList(songs, 0)
                        sharedViewModel.addRecentlyPlayed(songs[0].id)
                        sharedViewModel.player.play(songs[0])
                        mainViewModel.setFullScreenPlayer(true)
                        navController.navigate(Screen.Player.createRoute(songs[0].id))
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

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            items(songs) { song ->
                SongItemForGenre(
                    song = song,
                    onClick = {
                        sharedViewModel.setSongList(songs, songs.indexOf(song))
                        sharedViewModel.addRecentlyPlayed(song.id)
                        sharedViewModel.player.play(song)
                        mainViewModel.setFullScreenPlayer(true)
                        navController.navigate(Screen.Player.createRoute(song.id))
                    }
                )
            }
        }
    }
}

@Composable
fun SongItemForGenre(song: SongResponse, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = song.thumbnail,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(50.dp))
        IconButton(onClick = { /* Play action */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Play",
                tint = Color.White
            )
        }
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
    Log.d("HomeScreen", "PlayerSharedViewModel instance: ${sharedViewModel.hashCode()}")
    val state by viewModel.uiState.collectAsState()
    var tabIndex by rememberSaveable { mutableStateOf(0) }

    val tabs = listOf(
        TabItem("For you") { Home(viewModel, navController, mainViewModel, sharedViewModel) },
        TabItem("Relax") { Relax1(viewModel, navController, mainViewModel, sharedViewModel) },
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
        viewModel.loadArtist()
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