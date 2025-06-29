package com.example.musicapplicationse114.model

data class GenreResponse(val id: Long,
                 val name: String,
                 val description: String)

data class GenrePageResponse(val content : List<GenreResponse>)