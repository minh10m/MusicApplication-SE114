package com.example.musicapplicationse114.ui.screen.searchtype

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.musicapplicationse114.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchTypeScreen(
    navController: NavController,
    viewModel: SearchTypeViewModel,
    mainViewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
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

    Scaffold (){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 16.dp, vertical = 0.dp)
                .then(Modifier.padding(bottom = 129.dp))

        ) {
            SearchTypeTopBar(onBackClick = { navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            } })

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Recent searches",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.recentSearches) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, color = Color.White, fontSize = 14.sp)
                            Text(text = item.subtitle, color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove",
                            tint = Color.LightGray,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { viewModel.removeFromHistory(item) }
                        )
                    }
                }
            }

            Text(
                text = "Clear history",
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { viewModel.clearHistory() }
                    .padding(vertical = 80.dp)
            )
        }

    }
}

@Composable
fun SearchTypeTopBar(onBackClick: () -> Unit) {
    Spacer(modifier = Modifier.height(50.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                .clickable { onBackClick() }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Search songs, artist, album or playlist...",
            fontSize = 16.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun SearchBottomNavigationBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onHomeClick() }) {
                Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color.LightGray, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.height(3.dp))
                Text("Home", fontSize = 12.sp, color = Color.LightGray)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSearchClick() }) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.LightGray, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.height(3.dp))
                Text("Search", fontSize = 12.sp, color = Color.LightGray)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onLibraryClick() }) {
                Icon(Icons.Filled.Menu, contentDescription = "Library", tint = Color.LightGray, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.height(3.dp))
                Text("Library", fontSize = 12.sp, color = Color.LightGray)
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewSearchTypeScreen() {
    val navController = rememberNavController()
    MusicApplicationSE114Theme(darkTheme = true) {
        val navController = rememberNavController()
        SearchTypeScreen(
            navController = navController,
            viewModel = SearchTypeViewModel(null, null),
            mainViewModel = MainViewModel()
        )
    }
}
