package com.example.musicapplicationse114.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicapplicationse114.model.PlaylistResponse

@Suppress("UNUSED_PARAMETER")
@Composable
fun PlaylistSelectionDialog(
    songId: Long,
    onDismiss: () -> Unit,
    onPlaylistsSelected: (List<Long>) -> Unit = { _ -> }, // Unused callback for backward compatibility
    viewModel: PlaylistSelectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedPlaylistIds by remember { mutableStateOf(setOf<Long>()) }

    LaunchedEffect(Unit) {
        viewModel.loadMyPlaylistsAndCheckSong(songId)
    }

    // Initialize selected playlists with ones that already contain the song
    LaunchedEffect(uiState.playlistsContainingSong) {
        selectedPlaylistIds = uiState.playlistsContainingSong.toSet()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1C1C1C)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add to Playlist",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Loading or content
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    uiState.playlists.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No playlists found",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                    else -> {
                        // Playlists list
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(uiState.playlists) { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    isSelected = selectedPlaylistIds.contains(playlist.id),
                                    onSelectionChanged = { isSelected ->
                                        selectedPlaylistIds = if (isSelected) {
                                            selectedPlaylistIds + playlist.id
                                        } else {
                                            selectedPlaylistIds - playlist.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val playlistsToAdd = selectedPlaylistIds.filterNot { 
                                uiState.playlistsContainingSong.contains(it) 
                            }
                            val playlistsToRemove = uiState.playlistsContainingSong.filterNot {
                                selectedPlaylistIds.contains(it)
                            }
                            
                            if (playlistsToAdd.isNotEmpty() || playlistsToRemove.isNotEmpty()) {
                                viewModel.updateSongPlaylists(
                                    songId, 
                                    playlistsToAdd,
                                    playlistsToRemove
                                ) { message: String ->
                                    Toast.makeText(context, message as CharSequence, Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                            } else {
                                Toast.makeText(context, "No changes to save" as CharSequence, Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        enabled = selectedPlaylistIds != uiState.playlistsContainingSong,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        val addCount = selectedPlaylistIds.filterNot { 
                            uiState.playlistsContainingSong.contains(it) 
                        }.size
                        val removeCount = uiState.playlistsContainingSong.filterNot {
                            selectedPlaylistIds.contains(it)
                        }.size
                        
                        when {
                            addCount > 0 && removeCount > 0 -> Text("Update")
                            addCount > 0 -> Text("Add to $addCount playlist(s)")
                            removeCount > 0 -> Text("Remove from $removeCount playlist(s)")
                            else -> Text("Update")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    playlist: PlaylistResponse,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged(!isSelected) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = playlist.thumbnail ?: "https://via.placeholder.com/50",
            contentDescription = playlist.name,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (!playlist.description.isNullOrEmpty()) {
                Text(
                    text = playlist.description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = if (isSelected) "Selected" else "Not selected",
            tint = if (isSelected) Color.Green else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}
