package com.example.musicapplicationse114.ui.screen.comment

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.CommentResponse
import com.example.musicapplicationse114.model.SongResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentDialog(
    song: SongResponse,
    onDismiss: () -> Unit,
    viewModel: CommentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    LaunchedEffect(song.id) {
        viewModel.loadComments(song.id, refresh = true)
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status is LoadStatus.Error) {
            viewModel.clearStatus()
        }
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
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1C1C1C)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comments",
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.thumbnail,
                        contentDescription = "Song thumbnail",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artistName,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (uiState.isLoading && uiState.comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else if (uiState.comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No comments yet. Be the first to comment!",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        }
                    } else {
                        items(uiState.comments) { comment ->
                            CommentItem(
                                comment = comment,
                                onDeleteComment = { viewModel.deleteComment(it) },
                                onLikeComment = { commentId ->
                                    viewModel.likeComment(commentId)
                                },
                                onReplyComment = { commentId ->
                                    Log.d("CommentDebug", "Reply clicked for comment ID: $commentId")
                                    viewModel.startReply(commentId)
                                },
                                isReplying = uiState.replyingToCommentId == comment.id,
                                replyText = uiState.replyTexts[comment.id] ?: "",
                                onReplyTextChange = { commentId, text ->
                                    Log.d("CommentDebug", "Main comment text change: '$text' for comment $commentId")
                                    viewModel.updateReplyText(commentId, text)
                                },
                                onSubmitReply = { viewModel.submitReply(song.id) },
                                onCancelReply = viewModel::cancelReply,
                                likedCommentIds = uiState.likedCommentIds,
                                isNestedReply = false,
                                replyingToCommentId = uiState.replyingToCommentId
                            )
                        }

                        if (uiState.hasMorePages) {
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMoreComments(song.id)
                                }
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.padding(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = uiState.newCommentText,
                        onValueChange = viewModel::updateNewCommentText,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add a comment...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (uiState.newCommentText.trim().isNotEmpty()) {
                                    viewModel.addComment(song.id)
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (uiState.newCommentText.trim().isNotEmpty()) {
                                viewModel.addComment(song.id)
                                keyboardController?.hide()
                            }
                        },
                        enabled = uiState.newCommentText.trim().isNotEmpty() && !uiState.isLoading
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send comment",
                            tint = if (uiState.newCommentText.trim().isNotEmpty()) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CommentItem(
    comment: CommentResponse,
    onDeleteComment: (Long) -> Unit,
    onLikeComment: ((Long) -> Unit)? = null,
    onReplyComment: ((Long) -> Unit)? = null,
    isReplying: Boolean = false,
    replyText: String = "",
    onReplyTextChange: (Long, String) -> Unit = { _, _ -> },
    onSubmitReply: () -> Unit = {},
    onCancelReply: () -> Unit = {},
    likedCommentIds: Set<Long> = emptySet(),
    isNestedReply: Boolean = false,
    replyingToCommentId: Long? = null
) {
    val uiState by hiltViewModel<CommentViewModel>().uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User ${comment.userId}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    Text(
                        text = formatCommentDate(comment.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isLiked = likedCommentIds.contains(comment.id)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (onLikeComment != null) {
                                    Modifier.clickable { onLikeComment(comment.id) }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        if (comment.likes > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatLikeCount(comment.likes),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    if (onReplyComment != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onReplyComment(comment.id) }
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "Reply",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reply",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        if (isReplying) {
            Log.d("CommentDebug", "Showing reply box for comment ${comment.id}, replyText: '$replyText'")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (isNestedReply) 52.dp else 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2C2C2C)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Replying to User ${comment.userId}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { newText ->
                                Log.d("CommentDebug", "TextField onValueChange called for comment ${comment.id}, newText: '$newText'")
                                onReplyTextChange(comment.id, newText)
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Write a reply...", color = Color.Gray, fontSize = 14.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color.White
                            ),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = onCancelReply) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancel reply",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onSubmitReply,
                            enabled = replyText.trim().isNotEmpty()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send reply",
                                tint = if (replyText.trim().isNotEmpty()) Color.White else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        comment.replies?.let { replies ->
            if (replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.padding(start = 52.dp)
                ) {
                    replies.forEach { reply ->
                        Log.d("CommentDebug", "Rendering reply ${reply.id}, replyingTo: $replyingToCommentId, isReplying: ${replyingToCommentId == reply.id}")
                        CommentItem(
                            comment = reply,
                            onDeleteComment = onDeleteComment,
                            onLikeComment = onLikeComment,
                            onReplyComment = onReplyComment,
                            isReplying = replyingToCommentId == reply.id,
                            replyText = uiState.replyTexts[reply.id] ?: "",
                            onReplyTextChange = { commentId, text ->
                                onReplyTextChange(reply.id, text)
                            },
                            onSubmitReply = onSubmitReply,
                            onCancelReply = onCancelReply,
                            likedCommentIds = likedCommentIds,
                            isNestedReply = true,
                            replyingToCommentId = replyingToCommentId
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatCommentDate(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString.replace("Z", ""))
        val now = LocalDateTime.now()

        val minutesAgo = java.time.Duration.between(dateTime, now).toMinutes()

        when {
            minutesAgo < 1 -> "Just now"
            minutesAgo < 60 -> "${minutesAgo}m ago"
            minutesAgo < 1440 -> "${minutesAgo / 60}h ago"
            else -> "${minutesAgo / 1440}d ago"
        }
    } catch (e: DateTimeParseException) {
        "Recently"
    }
}

private fun formatLikeCount(likes: Long): String {
    return when {
        likes < 1000 -> likes.toString()
        likes < 1000000 -> String.format("%.1fK", likes / 1000.0).replace(".0K", "K")
        likes < 1000000000 -> String.format("%.1fM", likes / 1000000.0).replace(".0M", "M")
        else -> String.format("%.1fB", likes / 1000000000.0).replace(".0B", "B")
    }
}