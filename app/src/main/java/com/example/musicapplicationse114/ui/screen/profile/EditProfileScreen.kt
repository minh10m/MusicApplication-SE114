package com.example.musicapplicationse114.ui.screen.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.musicapplicationse114.R
import com.example.musicapplicationse114.Screen
import com.example.musicapplicationse114.ui.notification.LoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (profile == null) {
            viewModel.loadProfile()
        }
    }

    var username by remember { mutableStateOf(profile?.username ?: "") }
    var email by remember { mutableStateOf(profile?.email ?: "") }
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    // Kiểm tra xem có thay đổi nào không
    val isChanged = remember(username, email, phone, avatarUri, profile) {
        username.isNotBlank() || email.isNotBlank() || phone.isNotBlank() ||
                avatarUri.toString().isNotBlank()
                && (username != profile?.username ||
                        email != profile?.email ||
                        phone != profile?.phone ||
                        avatarUri != null)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
    }

    // Hiển thị Toast cho trạng thái cập nhật
    LaunchedEffect(updateState) {
        when (updateState) {
            is LoadState.Success -> {
                viewModel.loadProfile()
                Toast.makeText(context, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.EditProfile.route) { inclusive = true } // Xóa EditProfileScreen khỏi stack
                }
            }
            is LoadState.Error -> {
                Toast.makeText(context, (updateState as LoadState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    if(updateState is LoadState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp
            )
        }
    }else{
        Scaffold(
            containerColor = Color.Black
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Top row: back & save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.Profile.route) {
                                    popUpTo(Screen.EditProfile.route) { inclusive = true }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIos,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            "Edit Profile",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isChanged) Color.DarkGray else Color.Gray)
                            .clickable(enabled = isChanged) {
                                viewModel.updateProfile(
                                    username = username.ifBlank { profile?.username ?: "" },
                                    email = email.ifBlank { profile?.email ?: "" },
                                    phone = phone.ifBlank { profile?.phone ?: "" },
                                    avatarUri = avatarUri,
                                    context = context,
                                    onSuccess = {}
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Save",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar
                AsyncImage(
                    model = avatarUri ?: profile?.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap to change avatar", color = Color.LightGray, fontSize = 15.sp)

                Spacer(modifier = Modifier.height(24.dp))

                // Text fields
                EditTextField("Username", username) { username = it }
                Spacer(modifier = Modifier.height(12.dp))
                EditTextField("Email", email) { email = it }
                Spacer(modifier = Modifier.height(12.dp))
                EditTextField("Phone", phone) { phone = it }
            }
        }

    }
}

@Composable
fun EditTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                color = Color.LightGray,
                fontSize = 15.sp
            )
        },
        modifier = Modifier
            .fillMaxWidth(0.9f) // Ngắn hơn một chút
            .clip(RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.DarkGray,
            cursorColor = Color.White,
            focusedLabelColor = Color.White
        )
    )
}
