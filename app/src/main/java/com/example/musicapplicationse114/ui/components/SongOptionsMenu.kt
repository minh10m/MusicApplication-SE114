package com.example.musicapplicationse114.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.musicapplicationse114.model.SongResponse

@Composable
fun SongOptionsMenu(
    song: SongResponse,
    navController: NavController,
    isFavorite: Boolean,
    isDownloaded: Boolean,
    onFavoriteToggle: () -> Unit,
    onDownloadToggle: () -> Unit,
    onCommentClick: () -> Unit,
    onAddToPlaylists: ((Long, List<Long>) -> Unit)? = null, // songId, playlistIds
    onShare: ((Long) -> Unit)? = null, // songId parameter
    showGoToArtist: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                Icons.Default.MoreVert, 
                contentDescription = "More options", 
                tint = Color.White, 
                modifier = Modifier.size(32.dp)
            )
        }
        
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Color(0xFF2C2C2C))
        ) {
            // Favorite/Unfavorite
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                            color = Color.White
                        )
                    }
                },
                onClick = {
                    onFavoriteToggle()
                    showMenu = false
                }
            )
            
            // Comment
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.Comment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Comments", color = Color.White)
                    }
                },
                onClick = {
                    onCommentClick()
                    showMenu = false
                }
            )
            
            // Add to Playlist
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Add to Playlist", color = Color.White)
                    }
                },
                onClick = {
                    showPlaylistDialog = true
                    showMenu = false
                }
            )
            
            // Share
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Share", color = Color.White)
                    }
                },
                onClick = {
                    onShare?.invoke(song.id) ?: run {
                        Toast.makeText(context, "Share - Coming Soon", Toast.LENGTH_SHORT).show()
                    }
                    showMenu = false
                }
            )
            
            // Download
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = if (isDownloaded) Color.Green else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (isDownloaded) "Downloaded" else "Download",
                            color = Color.White
                        )
                    }
                },
                onClick = {
                    if (!isDownloaded) {
                        onDownloadToggle()
                        Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
                    }
                    showMenu = false
                },
                enabled = !isDownloaded
            )
            
            // Go to Artist
            if (showGoToArtist) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Go to Artist", color = Color.White)
                        }
                    },
                    onClick = {
                        navController.navigate("artist/${song.artistId}")
                        showMenu = false
                    }
                )
            }
        }
    }
    
    // Playlist selection dialog
    if (showPlaylistDialog) {
        PlaylistSelectionDialog(
            songId = song.id,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistsSelected = { playlistIds ->
                onAddToPlaylists?.invoke(song.id, playlistIds)
            }
        )
    }
}
