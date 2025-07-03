package com.example.musicapplicationse114.model

interface SessionCacheHandler {
    /** Xóa dữ liệu cache khi logout hoặc user chuyển tài khoản */
    fun clearSessionCache()

    /** Kiểm tra có dữ liệu cache không (để tránh gọi API lại) */
    fun hasSessionCache(): Boolean
}