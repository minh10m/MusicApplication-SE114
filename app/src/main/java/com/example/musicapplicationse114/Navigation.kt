package com.example.musicapplicationse114

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.musicapplicationse114.common.enum.TimeOfDay
import com.example.musicapplicationse114.ui.playerController.PlayerSharedViewModel
import com.example.musicapplicationse114.ui.queue.QueueScreen
import com.example.musicapplicationse114.ui.screen.album.AlbumSongListScreen
import com.example.musicapplicationse114.ui.screen.artist.ArtistSongListScreen
import com.example.musicapplicationse114.ui.screen.detail.DetailScreen
import com.example.musicapplicationse114.ui.screen.home.HomeScreen
import com.example.musicapplicationse114.ui.screen.home.HomeViewModel
import com.example.musicapplicationse114.ui.screen.library.LibraryScreen
import com.example.musicapplicationse114.ui.screen.login.LoginScreen
import com.example.musicapplicationse114.ui.screen.login.LoginViewModel
import com.example.musicapplicationse114.ui.screen.player.MiniPlayer
import com.example.musicapplicationse114.ui.screen.player.PlayerScreen
import com.example.musicapplicationse114.ui.screen.searchtype.SearchTypeScreen
import com.example.musicapplicationse114.ui.screen.signUp.SignUpScreen
import com.example.musicapplicationse114.ui.screen.start.StartScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Login : Screen("login", "Login")
    object SignUp : Screen("signup", "Sign Up")
    object Detail : Screen("detail", "Detail")
    object Start : Screen("start", "Start")
    object Search : Screen("search", "Search")
    object Library : Screen("library", "Library")
    object Player : Screen("player/{songId}", "Player") {
        fun createRoute(songId: Long) = "player/$songId"
    }
    object Album: Screen("album/{albumId}", "Album")
    {
        fun createRoute(albumId: Long) = "album/$albumId"
    }
    object Artist: Screen("artist/{artistId}", "Artist")
    {
        fun createRoute(artistId: Long) = "artist/$artistId"
    }
    object Queue: Screen("queue", "Queue")

}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Navigation() {
    val navController = rememberAnimatedNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val mainState = mainViewModel.uiState.collectAsState()

    val sharedViewModel: PlayerSharedViewModel = hiltViewModel()
    val globalPlayerController = sharedViewModel.player
    val playerState by globalPlayerController.state.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(mainState.value.error) {
        if (mainState.value.error.isNotEmpty()) {
            Toast.makeText(context, mainState.value.error, Toast.LENGTH_SHORT).show()
            mainViewModel.setError("")
        }
    }

    Scaffold(
        containerColor = Color.Black,
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                AnimatedNavHost(
                    navController = navController,
                    startDestination = Screen.Start.route,
                    enterTransition = { fadeIn(animationSpec = tween(5)) },
                    exitTransition = { fadeOut(animationSpec = tween(5)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(5)) },
                    popExitTransition = { fadeOut(animationSpec = tween(5)) }
                ) {
                    composable(Screen.Start.route) {
                        StartScreen(
                            navController = navController,
                            viewModel = hiltViewModel(),
                            mainViewModel
                        )
                    }
                    composable(Screen.Login.route) {
                        LoginScreen(
                            navController = navController,
                            viewModel = hiltViewModel(),
                            mainViewModel,
                            homeViewModel = hiltViewModel()
                        )
                    }
                    composable(Screen.SignUp.route) {
                        SignUpScreen(
                            navController = navController,
                            viewModel = hiltViewModel(),
                            mainViewModel
                        )
                    }
                    composable(
                        "home?username={username}&timeOfDay={timeOfDay}",
                        arguments = listOf(
                            navArgument("username") { defaultValue = "" },
                            navArgument("timeOfDay") { defaultValue = "MORNING" }
                        )
                    ) { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("username") ?: ""
                        Log.i("usernameNavigation", username)
                        val timeOfDayStr = backStackEntry.arguments?.getString("timeOfDay") ?: "MORNING"
                        val timeOfDay = TimeOfDay.valueOf(timeOfDayStr)
                        HomeScreen(
                            navController = navController,
                            viewModel = hiltViewModel(),
                            mainViewModel,
                            username = username
                        )
                    }
                    composable(Screen.Detail.route) {
                        DetailScreen()
                    }
                    composable(Screen.Search.route) {
                        SearchTypeScreen(
                            navController = navController,
                            viewModel = hiltViewModel(),
                            mainViewModel
                        )
                    }
                    composable(Screen.Library.route) {
                        LibraryScreen(
                            navController = navController,
                            viewModel = hiltViewModel(),
                            mainViewModel
                        )
                    }
                    composable(Screen.Queue.route){
                        QueueScreen(
                            navController = navController,
                            viewModel = hiltViewModel(),
                            sharedViewModel,
                            mainViewModel
                        )
                    }
                    composable(
                        route = Screen.Artist.route,
                        arguments = listOf(navArgument("artistId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val artistId = backStackEntry.arguments?.getLong("artistId")
                        if(artistId != null)
                        {
                            ArtistSongListScreen(
                                navController = navController,
                                artistId = artistId,
                                viewModel = hiltViewModel(),
                                mainViewModel = hiltViewModel()
                            )
                        }
                        else
                        {
                            Log.e("Navigation", "Artist ID is null for ArtistSongList screen")
                            navController.popBackStack()
                        }
                    }
                    composable(
                        route = Screen.Album.route,
                        arguments = listOf(navArgument("albumId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getLong("albumId")
                        if(albumId != null)
                        {
                            AlbumSongListScreen(
                                navController = navController,
                                albumId = albumId,
                                viewModel = hiltViewModel(),
                                mainViewModel = hiltViewModel()
                            )
                        }
                        else
                        {
                            Log.e("Navigation", "Album ID is null for AlbumSongList screen")
                            navController.popBackStack()
                        }
                    }
                    composable(
                        route = Screen.Player.route,
                        arguments = listOf(navArgument("songId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val songId = backStackEntry.arguments?.getLong("songId")
                        if (songId != null) {
                            PlayerScreen(
                                navController = navController,
                                songId = songId,
                                viewModel = hiltViewModel(),
                                mainViewModel,
                                sharedViewModel
                            )
                        } else {
                            Log.e("Navigation", "Song ID is null for Player screen")
                            navController.popBackStack()
                        }
                    }
                }
            }
        },
        bottomBar = {
            val currentRoute = currentRoute(navController)
            // Chỉ hiển thị bottomBar ở các màn hình Home, Search, Library
            if (currentRoute in listOf(Screen.Home.route, Screen.Album.route, Screen.Artist.route, Screen.Search.route, Screen.Library.route, "home?username={username}&timeOfDay={timeOfDay}")) {
                Column {
                    if (playerState.currentSong != null && !mainViewModel.isFullScreenPlayer.value) {
                        MiniPlayer(
                            song = playerState.currentSong!!,
                            isPlaying = playerState.isPlaying,
                            onClick = {
                                mainViewModel.setFullScreenPlayer(true)
                                navController.navigate(Screen.Player.createRoute(playerState.currentSong!!.id))
                            },
                            onToggle = {
                                globalPlayerController.toggle()
                            }
                        )
                    }
                    Box(modifier = Modifier.padding(bottom = 30.dp))
                    {
                        NavigationBar(navController)
                    }
                }
            }
        }
    )
}

@Composable
fun NavigationBar(navController: NavController) {
    val currentRoute = currentRoute(navController)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Filled.Home,
                label = "Home",
                selected = currentRoute == Screen.Home.route || currentRoute == "home?username={username}&timeOfDay={timeOfDay}",
                onClick = {
                    if (currentRoute != Screen.Home.route && currentRoute != "home?username={username}&timeOfDay={timeOfDay}") {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        Log.d("NavigationBar", "Navigated to Home")
                    }
                }
            )
            NavBarItem(
                icon = Icons.Filled.Search,
                label = "Search",
                selected = currentRoute == Screen.Search.route,
                onClick = {
                    navController.navigate(Screen.Search.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    Log.d("NavigationBar", "Navigated to Search")
                }
            )
            NavBarItem(
                icon = Icons.Filled.LibraryMusic,
                label = "Library",
                selected = currentRoute == Screen.Library.route,
                onClick = {
                    navController.navigate(Screen.Library.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    Log.d("NavigationBar", "Navigated to Library")
                }
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Color.White else Color.LightGray.copy(alpha = 0.5f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(30.dp))
        }
        Text(label, fontSize = 12.sp, color = color)
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}