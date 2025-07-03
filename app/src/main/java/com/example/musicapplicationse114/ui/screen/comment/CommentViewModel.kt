package com.example.musicapplicationse114.ui.screen.comment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapplicationse114.common.enum.LoadStatus
import com.example.musicapplicationse114.model.CommentRequest
import com.example.musicapplicationse114.model.CommentResponse
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.auth.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentUiState(
    val comments: List<CommentResponse> = emptyList(),
    val status: LoadStatus = LoadStatus.Init(),
    val newCommentText: String = "",
    val isLoading: Boolean = false,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    val replyingToCommentId: Long? = null,
    val replyText: String = "",
    val likedCommentIds: Set<Long> = emptySet()
)

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val api: Api,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState = _uiState.asStateFlow()

    fun loadComments(songId: Long, refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (refresh) {
                    _uiState.value = _uiState.value.copy(
                        currentPage = 0,
                        comments = emptyList(),
                        hasMorePages = true
                    )
                }
                
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Authentication token not found"),
                        isLoading = false
                    )
                    return@launch
                }

                Log.d("CommentViewModel", "Loading comments for songId: $songId")

                // Load comments and liked comments simultaneously
                val commentsResponse = api.getCommentsBySongId(
                    token = token,
                    songId = songId
                )
                
                val userId = getUserId()
                val likedCommentsResponse = api.getUserLikedComments(
                    token = token,
                    songId = songId,
                    userId = userId
                )

                Log.d("CommentViewModel", "Comments response code: ${commentsResponse.code()}")
                Log.d("CommentViewModel", "Liked comments response code: ${likedCommentsResponse.code()}")

                if (commentsResponse.isSuccessful) {
                    val comments = commentsResponse.body() ?: emptyList()
                    val likedCommentIds = if (likedCommentsResponse.isSuccessful) {
                        likedCommentsResponse.body()?.toSet() ?: emptySet()
                    } else {
                        emptySet()
                    }
                    
                    Log.d("CommentViewModel", "Loaded ${comments.size} comments")
                    Log.d("CommentViewModel", "Liked comments: $likedCommentIds")
                    
                    _uiState.value = _uiState.value.copy(
                        comments = comments,
                        likedCommentIds = likedCommentIds,
                        status = LoadStatus.Success(),
                        isLoading = false,
                        hasMorePages = false // Backend returns all comments at once
                    )
                } else {
                    Log.e("CommentViewModel", "Failed to load comments: ${commentsResponse.errorBody()?.string()}")
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Failed to load comments: ${commentsResponse.code()}"),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("CommentViewModel", "Error loading comments", e)
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error("Error loading comments: ${e.message}"),
                    isLoading = false
                )
            }
        }
    }

    fun loadMoreComments(songId: Long) {
        if (_uiState.value.hasMorePages && !_uiState.value.isLoading) {
            loadComments(songId, refresh = false)
        }
    }

    fun updateNewCommentText(text: String) {
        _uiState.value = _uiState.value.copy(newCommentText = text)
    }

    fun addComment(songId: Long) {
        val commentText = _uiState.value.newCommentText.trim()
        if (commentText.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val request = CommentRequest(
                    content = commentText,
                    userId = getUserId() // Get current user ID
                )

                Log.d("CommentViewModel", "Sending comment: $commentText for songId: $songId")

                val response = api.addComment(
                    token = tokenManager.getToken(),
                    songId = songId,
                    request = request
                )

                Log.d("CommentViewModel", "Response code: ${response.code()}")
                Log.d("CommentViewModel", "Response body: ${response.body()}")

                if (response.isSuccessful) {
                    val newComment = response.body()
                    if (newComment != null) {
                        Log.d("CommentViewModel", "Successfully added comment: ${newComment.content}")
                        // Add new comment to the top of the list
                        val updatedComments = listOf(newComment) + _uiState.value.comments
                        _uiState.value = _uiState.value.copy(
                            comments = updatedComments,
                            newCommentText = "",
                            status = LoadStatus.Success(),
                            isLoading = false
                        )
                        
                        // Reload comments to ensure consistency
                        loadComments(songId, refresh = true)
                    }
                } else {
                    Log.e("CommentViewModel", "Failed to add comment: ${response.errorBody()?.string()}")
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Failed to add comment: ${response.code()}"),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(e.message ?: "Unknown error occurred"),
                    isLoading = false
                )
            }
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Authentication token not found")
                    )
                    return@launch
                }

                val response = api.deleteComment(
                    token = token,
                    commentId = commentId
                )

                if (response.isSuccessful) {
                    // Remove comment from the list
                    val updatedComments = _uiState.value.comments.filter { it.id != commentId }
                    _uiState.value = _uiState.value.copy(
                        comments = updatedComments,
                        status = LoadStatus.Success()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Failed to delete comment")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error(e.message ?: "Unknown error occurred")
                )
            }
        }
    }

    private suspend fun getUserId(): Long {
        return tokenManager.getUserId()
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(status = LoadStatus.Init())
    }

    fun startReply(commentId: Long) {
        Log.d("CommentViewModel", "startReply called for commentId: $commentId")
        _uiState.value = _uiState.value.copy(
            replyingToCommentId = commentId,
            replyText = ""
        )
        Log.d("CommentViewModel", "State updated - replyingToCommentId: ${_uiState.value.replyingToCommentId}, replyText: '${_uiState.value.replyText}'")
    }

    fun cancelReply() {
        Log.d("CommentViewModel", "cancelReply called")
        _uiState.value = _uiState.value.copy(
            replyingToCommentId = null,
            replyText = ""
        )
    }

    fun updateReplyText(text: String) {
        Log.d("CommentViewModel", "updateReplyText called with: '$text', current replyingTo: ${_uiState.value.replyingToCommentId}")
        _uiState.value = _uiState.value.copy(replyText = text)
        Log.d("CommentViewModel", "State updated - replyText: '${_uiState.value.replyText}'")
    }

    fun submitReply(songId: Long) {
        val replyText = _uiState.value.replyText.trim()
        val replyingToCommentId = _uiState.value.replyingToCommentId
        
        if (replyText.isEmpty() || replyingToCommentId == null) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Find the parent comment ID (for nested replies, use the root parent)
                val parentCommentId = findRootParentId(replyingToCommentId)

                val request = CommentRequest(
                    content = replyText,
                    userId = getUserId(),
                    parentId = parentCommentId
                )

                Log.d("CommentViewModel", "Submitting reply to commentId: $replyingToCommentId, parentId: $parentCommentId")

                val response = api.addComment(
                    token = tokenManager.getToken(),
                    songId = songId,
                    request = request
                )

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        replyingToCommentId = null,
                        replyText = "",
                        isLoading = false
                    )
                    // Reload comments to show the new reply
                    loadComments(songId, refresh = true)
                } else {
                    Log.e("CommentViewModel", "Failed to add reply: ${response.errorBody()?.string()}")
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Failed to add reply"),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("CommentViewModel", "Error adding reply", e)
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error("Error adding reply: ${e.message}"),
                    isLoading = false
                )
            }
        }
    }

    private fun findRootParentId(commentId: Long): Long {
        // Find if this comment is already a reply (has parentId)
        for (comment in _uiState.value.comments) {
            if (comment.id == commentId) {
                // This is a top-level comment, so it becomes the parent
                return commentId
            }
            comment.replies?.forEach { reply ->
                if (reply.id == commentId) {
                    // This is a reply to a comment, so use the parent comment as root
                    return comment.id
                }
            }
        }
        // Fallback: return the commentId itself
        return commentId
    }

    fun likeComment(commentId: Long) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Authentication token not found")
                    )
                    return@launch
                }

                // Find the comment to get songId (search in both main comments and replies)
                val comment = findCommentById(commentId)
                if (comment == null) {
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Comment not found")
                    )
                    return@launch
                }

                val isCurrentlyLiked = _uiState.value.likedCommentIds.contains(commentId)
                
                // Don't allow liking an already liked comment or unliking a not-liked comment
                if (isCurrentlyLiked) {
                    // Unlike the comment
                    val response = api.unlikeComment(
                        token = token,
                        songId = comment.songId,
                        commentId = commentId,
                        userId = getUserId()
                    )
                    
                    if (response.isSuccessful) {
                        val updatedLikedIds = _uiState.value.likedCommentIds - commentId
                        val updatedComments = updateCommentLikes(_uiState.value.comments, commentId, -1)
                        
                        _uiState.value = _uiState.value.copy(
                            comments = updatedComments,
                            likedCommentIds = updatedLikedIds
                        )
                    } else {
                        Log.e("CommentViewModel", "Failed to unlike comment: ${response.errorBody()?.string()}")
                        _uiState.value = _uiState.value.copy(
                            status = LoadStatus.Error("Failed to unlike comment")
                        )
                    }
                } else {
                    // Like the comment
                    val response = api.likeComment(
                        token = token,
                        songId = comment.songId,
                        commentId = commentId,
                        userId = getUserId()
                    )
                    
                    if (response.isSuccessful) {
                        val updatedLikedIds = _uiState.value.likedCommentIds + commentId
                        val updatedComments = updateCommentLikes(_uiState.value.comments, commentId, 1)
                        
                        _uiState.value = _uiState.value.copy(
                            comments = updatedComments,
                            likedCommentIds = updatedLikedIds
                        )
                    } else {
                        Log.e("CommentViewModel", "Failed to like comment: ${response.errorBody()?.string()}")
                        _uiState.value = _uiState.value.copy(
                            status = LoadStatus.Error("Failed to like comment")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("CommentViewModel", "Error liking/unliking comment", e)
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error("Error liking/unliking comment: ${e.message}")
                )
            }
        }
    }

    private fun findCommentById(commentId: Long): CommentResponse? {
        for (comment in _uiState.value.comments) {
            if (comment.id == commentId) return comment
            comment.replies?.forEach { reply ->
                if (reply.id == commentId) return reply
            }
        }
        return null
    }

    private fun updateCommentLikes(
        comments: List<CommentResponse>, 
        commentId: Long, 
        likeChange: Int
    ): List<CommentResponse> {
        return comments.map { comment ->
            if (comment.id == commentId) {
                comment.copy(likes = (comment.likes + likeChange).coerceAtLeast(0))
            } else {
                comment.copy(
                    replies = comment.replies?.map { reply ->
                        if (reply.id == commentId) {
                            reply.copy(likes = (reply.likes + likeChange).coerceAtLeast(0))
                        } else {
                            reply
                        }
                    }
                )
            }
        }
    }

    fun replyToComment(commentId: Long, content: String) {
        if (content.trim().isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val request = CommentRequest(
                    content = content.trim(),
                    userId = getUserId(),
                    parentId = commentId
                )

                val response = api.addComment(
                    token = tokenManager.getToken(),
                    songId = _uiState.value.comments.firstOrNull()?.songId ?: 0L,
                    request = request
                )

                if (response.isSuccessful) {
                    val newReply = response.body()
                    if (newReply != null) {
                        // Reload comments to show the new reply
                        loadComments(newReply.songId, refresh = true)
                    }
                } else {
                    Log.e("CommentViewModel", "Failed to add reply: ${response.errorBody()?.string()}")
                    _uiState.value = _uiState.value.copy(
                        status = LoadStatus.Error("Failed to add reply"),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("CommentViewModel", "Error adding reply", e)
                _uiState.value = _uiState.value.copy(
                    status = LoadStatus.Error("Error adding reply: ${e.message}"),
                    isLoading = false
                )
            }
        }
    }
}
