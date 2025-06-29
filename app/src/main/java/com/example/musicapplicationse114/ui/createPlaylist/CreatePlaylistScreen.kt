package com.example.musicapplicationse114.ui.createPlaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.ui.screen.playlists.PlayListViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistScreen(
    navController: NavController,
    viewModel: CreatePlaylistViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
    playlistViewModel: PlayListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLoading by remember { mutableStateOf(false) }

    if (showLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status is LoadStatus.Success) {
            playlistViewModel.loadPlaylist()
            navController.navigate("playlist") {
                popUpTo("createPlaylist") { inclusive = true }
            }
        }
    }

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
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (!navController.popBackStack()) {
                                navController.navigate("playlist")
                            }
                        }
                )

                Spacer(modifier = Modifier.width(26.dp))

                Text(
                    text = "Create Playlist",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name Input
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Playlist Name", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Input
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // isPublic Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Public Playlist", color = Color.White)
                Switch(
                    checked = uiState.isPublic,
                    onCheckedChange = { viewModel.updateIsPublic(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1DB954),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    showLoading = true
                    viewModel.createPlaylist()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Create Playlist", color = Color.White)
            }
        }
    }
}
