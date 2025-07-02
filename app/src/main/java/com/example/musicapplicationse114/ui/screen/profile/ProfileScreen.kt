package com.example.musicapplicationse114.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.model.ProfileDto
import com.example.musicapplicationse114.ui.notification.LoadState
import com.example.musicapplicationse114.ui.theme.MusicApplicationSE114Theme

@Composable
fun ProfileScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val loadState by viewModel.loadState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(logoutState) {
        if (logoutState is LoadState.Success) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true } // Xóa toàn bộ navigation stack
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
//                NavigationBar(navController = navController) { }
            }
        }
    ) { innerPadding ->
        when (loadState) {
            is LoadState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is LoadState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (loadState as LoadState.Error).message,
                        color = Color.Red
                    )
                }
            }
            is LoadState.Success -> {
                profile?.let { ProfileContent(it, innerPadding, viewModel, navController) }
            }
            else -> Unit
        }
    }
}

@Composable
fun ProfileContent(profile: ProfileDto, innerPadding: PaddingValues, viewModel: ProfileViewModel, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 24.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        "My Profile",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Right: Edit button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray)
                        .clickable {
                            navController.navigate(Screen.EditProfile.route)

                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Edit",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AsyncImage(
                model = profile.avatar,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                profile.username,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProfileInfoRow(label = "Email", value = profile.email)
            ProfileInfoRow(label = "Phone Number", value = profile.phone)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatBox(icon = Icons.Default.Favorite, value = profile.favoriteSongCount.toString(), label = "songs")
                ProfileStatBox(icon = Icons.Default.QueueMusic, value = profile.playlistCount.toString(), label = "playlists")
                ProfileStatBox(icon = Icons.Default.Group, value = profile.followedArtistCount.toString(), label = "artists")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
                    .clickable {
                        viewModel.logout()
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sign Out",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}



@Composable
fun ProfileStatBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        modifier = Modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .width(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "$value $label",
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color.Gray
        )
        Text(
            value,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewProfileScreen() {
    MusicApplicationSE114Theme(darkTheme = true) {
        ProfileScreen(
            navController = rememberNavController(),
            mainViewModel = MainViewModel()
        )
    }
}
