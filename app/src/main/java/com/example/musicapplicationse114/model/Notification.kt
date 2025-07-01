package com.example.musicapplicationse114.model

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    val id: Long, // Hoặc String nếu server trả về chuỗi
    val title: String,

    @SerializedName("content")
    val message: String, // match với "content" bên Java

    val type: String,

    @SerializedName("read")
    val isRead: Boolean,

    val createdAt: String, // vẫn được nếu bạn xử lý parse sau

    val metadata: Map<String, String>? = null
)

