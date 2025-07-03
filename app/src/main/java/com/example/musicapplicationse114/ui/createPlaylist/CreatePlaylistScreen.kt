package com.example.musicapplicationse114.ui.createPlaylist

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.musicapplicationse114.MainViewModel
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.ui.screen.playlists.PlayListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistScreen(
    navController: NavController,
    viewModel: CreatePlaylistViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
    playlistViewModel: PlayListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLoading by remember { mutableStateOf(false) }

    if (showLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(strokeWidth = 2.dp,
                color = Color.White)
        }
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status is LoadStatus.Success) {
            Toast.makeText(context, "Tạo playlist thành công", Toast.LENGTH_SHORT).show()
            playlistViewModel.loadPlaylist()
            navController.navigate("playlist") {
                // Pop để tránh duplicate stack (nếu cần)
                popUpTo("createPlaylist") { inclusive = true }
            }
        }
    }

    if(uiState.status is LoadStatus.Loading)
    {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = Color.White
            )
        }
    }
    else {
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
                        tint = Color(0xFFAAAAAA), // Màu icon nhẹ hơn
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
                        text = "Tạo playlist",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White // Màu tiêu đề nhẹ hơn
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Name Input
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Tên playlist", color = Color.White) }, // Màu nhãn nhẹ hơn
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color(0xFFAAAAAA), // Màu văn bản khi focus
                        unfocusedTextColor = Color(0xFF888888), // Màu văn bản khi không focus
                        cursorColor = Color(0xFFAAAAAA), // Màu con trỏ nhẹ hơn
                        focusedBorderColor = Color(0xFF555555), // Viền khi focus tối hơn
                        unfocusedBorderColor = Color(0xFF333333), // Viền khi không focus tối hơn
                        focusedLabelColor = Color(0xFFAAAAAA), // Màu nhãn khi focus
                        unfocusedLabelColor = Color(0xFF888888) // Màu nhãn khi không focus
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description Input
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Mô tả", color = Color.White) }, // Màu nhãn nhẹ hơn
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color(0xFFAAAAAA), // Màu văn bản khi focus
                        unfocusedTextColor = Color(0xFF888888), // Màu văn bản khi không focus
                        cursorColor = Color(0xFFAAAAAA), // Màu con trỏ nhẹ hơn
                        focusedBorderColor = Color(0xFF555555), // Viền khi focus tối hơn
                        unfocusedBorderColor = Color(0xFF333333), // Viền khi không focus tối hơn
                        focusedLabelColor = Color(0xFFAAAAAA), // Màu nhãn khi focus
                        unfocusedLabelColor = Color(0xFF888888) // Màu nhãn khi không focus
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // isPublic Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Công khai", color = Color.White) // Màu text nhẹ hơn
                    Switch(
                        checked = uiState.isPublic,
                        onCheckedChange = { viewModel.updateIsPublic(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White, // Màu thumb khi bật
                            checkedTrackColor = Color(0xFF4169E1), // Giữ màu xanh đậm
                            uncheckedThumbColor = Color.White, // Màu thumb khi tắt
                            uncheckedTrackColor = Color(0xFF333333) // Màu track khi tắt
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                val name = uiState.name
                val description = uiState.description
                val bool = if (name.isNotBlank()) true else false
                Button(
                    onClick = {
                        showLoading = true
                        if (bool) viewModel.createPlaylist()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = if (bool) ButtonDefaults.buttonColors(containerColor = Color(0xFF4169E1)) else
                        ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) {
                    Text(
                        "Tạo playlist mới",
                        color = Color.White,
                        fontSize = 19.sp
                    ) // Màu text nút nhẹ hơn
                }
            }
        }
    }
}