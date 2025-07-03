package com.example.musicapplicationse114.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class CommentResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("songId")
    val songId: Long,
    
    @SerializedName("userId")
    val userId: Long,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("parentId")
    val parentId: Long? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("likes")
    val likes: Long = 0L,
    
    @SerializedName("replies")
    val replies: List<CommentResponse>? = null
)

data class CommentRequest(
    @SerializedName("content")
    val content: String,
    
    @SerializedName("userId")
    val userId: Long,
    
    @SerializedName("parentId")
    val parentId: Long? = null
)

data class CommentPageResponse(
    @SerializedName("content")
    val content: List<CommentResponse>,
    
    @SerializedName("pageable")
    val pageable: PageableResponse,
    
    @SerializedName("totalElements")
    val totalElements: Long,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("number")
    val number: Int,
    
    @SerializedName("numberOfElements")
    val numberOfElements: Int,
    
    @SerializedName("first")
    val first: Boolean,
    
    @SerializedName("last")
    val last: Boolean,
    
    @SerializedName("empty")
    val empty: Boolean
)

data class PageableResponse(
    @SerializedName("pageNumber")
    val pageNumber: Int,
    
    @SerializedName("pageSize")
    val pageSize: Int,
    
    @SerializedName("sort")
    val sort: SortResponse,
    
    @SerializedName("offset")
    val offset: Long,
    
    @SerializedName("paged")
    val paged: Boolean,
    
    @SerializedName("unpaged")
    val unpaged: Boolean
)

data class SortResponse(
    @SerializedName("empty")
    val empty: Boolean,
    
    @SerializedName("sorted")
    val sorted: Boolean,
    
    @SerializedName("unsorted")
    val unsorted: Boolean
)
