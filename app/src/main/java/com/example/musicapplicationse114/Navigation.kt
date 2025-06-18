package com.example.musicapplicationse114

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.common.enum.TimeOfDay
import com.example.musicapplicationse114.ui.playerController.PlayerSharedViewModel
import com.example.musicapplicationse114.ui.screen.detail.DetailScreen
import com.example.musicapplicationse114.ui.screen.home.HomeScreen
import com.example.musicapplicationse114.ui.screen.home.HomeViewModel
import com.example.musicapplicationse114.ui.screen.library.LibraryScreen
import com.example.musicapplicationse114.ui.screen.login.LoginScreen
import com.example.musicapplicationse114.ui.screen.login.LoginViewModel
import com.example.musicapplicationse114.ui.screen.player.MiniPlayer
import com.example.musicapplicationse114.ui.screen.player.PlayerScreen
import com.example.musicapplicationse114.ui.screen.search.SearchScreenStyled
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
        object Player : Screen("player/{songId}", "Player")
        {
            fun createRoute(songId: Long) = "player/$songId"
        }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Navigation() {
    val navController = rememberAnimatedNavController()
    val mainViewModel : MainViewModel = hiltViewModel()
    val loginViewModel : LoginViewModel = hiltViewModel()
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
        bottomBar = {
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
        }
    ) { innerPadding ->
        AnimatedNavHost(navController = navController, startDestination = Screen.Start.route,
            enterTransition = {
                fadeIn(animationSpec = tween(5))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(5))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(5))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(5))
            })
        {
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
            composable("home?username={username}&timeOfDay={timeOfDay}",
                arguments = listOf(
                    navArgument("username") {
                        defaultValue = ""
                    },
                    navArgument("timeOfDay") {
                        defaultValue = "MORNING"
                    }
                )) { backStackEntry ->
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
            composable(
                route = Screen.Player.route,
                arguments = listOf(navArgument("songId") { type = NavType.LongType })
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getLong("songId")
                if (songId != null) {
                    PlayerScreen(
                        navController = navController,
                        songId = songId, // Truyền songId vào PlayerScreen
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
}