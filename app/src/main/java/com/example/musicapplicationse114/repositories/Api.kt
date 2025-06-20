package com.example.musicapplicationse114.repositories

import com.example.musicapplicationse114.model.AddFavoriteSongRequest
import com.example.musicapplicationse114.model.AlbumPageResponse
import com.example.musicapplicationse114.model.AuthenticationResponse
import com.example.musicapplicationse114.model.DownloadedSongPageResponse
import com.example.musicapplicationse114.model.DownloadedSongResponse
import com.example.musicapplicationse114.model.FavoriteSongPageResponse
import com.example.musicapplicationse114.model.FavoriteSongResponse
import com.example.musicapplicationse114.model.SongPageResponse
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.model.UserLoginRequest
import com.example.musicapplicationse114.model.UserSignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {
//    suspend fun login1(username: String, password:String):Boolean
//    suspend fun signUp1(username: String, email: String, password: String, confirmPassword: String):Boolean
//    suspend fun loadAlbums():ArrayList<Album>
//    suspend fun loadSong():ArrayList<Song>
//    suspend fun loadRecentPlayed():ArrayList<RecentlyPlayed>

    @POST("/login")
    suspend fun login(@Body request : UserLoginRequest): Response<AuthenticationResponse>

    @POST("/register")
    suspend fun register(@Body request: UserSignUpRequest): Response<AuthenticationResponse>

    @GET("api/albums")
    suspend fun getAlbums(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): AlbumPageResponse

    @GET("api/songs")
    suspend fun getSongs(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): SongPageResponse

    @GET("api/songs/{id}")
    suspend fun getSongById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
        ) : SongResponse

    @POST("api/favorite-songs")
    suspend fun addFavoriteSong(
        @Header("Authorization") token: String,
        @Body request: AddFavoriteSongRequest
    ) : FavoriteSongResponse

    @GET("api/favorite-songs/user/{userId}")
    suspend fun getFavoriteSongs(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "addedAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<FavoriteSongPageResponse>

    @DELETE("api/favorite-songs/{id}")
    suspend fun removeFavoriteSong(
        @Header("Authorization") token: String,
        @Path("id") favoriteSongId: Long
    ): Response<Void>


    @POST("api/downloaded-songs")
    suspend fun addDownloadedSong(
        @Header("Authorization") token: String,
        @Query("userId") userId: Long,
        @Query("songId") songId: Long
    ): Response<DownloadedSongResponse>


    @GET("api/downloaded-songs/user/{userId}")
    suspend fun getDownloadedSongs(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<DownloadedSongPageResponse>


    @DELETE("api/downloaded-songs/{id}")
    suspend fun removeDownloadedSong(
        @Header("Authorization") token: String,
        @Path("id") downloadedSongId: Long
    ): Response<Void>
}