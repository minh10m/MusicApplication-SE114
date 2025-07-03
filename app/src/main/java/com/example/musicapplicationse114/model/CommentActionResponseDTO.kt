package com.example.musicapplicationse114.model

data class CommentActionResponseDTO(
    val success: Boolean,
    val message: String,
    val comment: CommentResponse?
)
