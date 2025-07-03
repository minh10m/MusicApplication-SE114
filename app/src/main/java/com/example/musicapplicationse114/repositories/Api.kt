package com.example.musicapplicationse114.repositories

import com.example.musicapplicationse114.model.AddFavoriteSongRequest
import com.example.musicapplicationse114.model.AlbumPageResponse
import com.example.musicapplicationse114.model.AlbumResponse
import com.example.musicapplicationse114.model.ArtistPageResponse
import com.example.musicapplicationse114.model.ArtistResponse
import com.example.musicapplicationse114.model.AuthenticationResponse
import com.example.musicapplicationse114.model.ChangePasswordRequest
import com.example.musicapplicationse114.model.CommentActionResponseDTO
import com.example.musicapplicationse114.model.CommentPageResponse
import com.example.musicapplicationse114.model.CommentRequest
import com.example.musicapplicationse114.model.CommentResponse
import com.example.musicapplicationse114.model.DownloadedSongPageResponse
import com.example.musicapplicationse114.model.DownloadedSongResponse
import com.example.musicapplicationse114.model.FavoriteSongPageResponse
import com.example.musicapplicationse114.model.FavoriteSongResponse
import com.example.musicapplicationse114.model.FollowArtistPageResponse
import com.example.musicapplicationse114.model.FollowArtistRequest
import com.example.musicapplicationse114.model.FollowArtistResponse
import com.example.musicapplicationse114.model.GenrePageResponse
import com.example.musicapplicationse114.model.GlobalSearchResultDTO
import com.example.musicapplicationse114.model.NotificationDto
import com.example.musicapplicationse114.model.PlaylistPageResponse
import com.example.musicapplicationse114.model.PlaylistRequest
import com.example.musicapplicationse114.model.PlaylistResponse
import com.example.musicapplicationse114.model.RecentlyPlayedPageResponse
import com.example.musicapplicationse114.model.SongPageResponse
import com.example.musicapplicationse114.model.SongPlaylist
import com.example.musicapplicationse114.model.SongPlaylistDTO
import com.example.musicapplicationse114.model.SongPlaylistRequest
import com.example.musicapplicationse114.model.SongResponse
import com.example.musicapplicationse114.model.SongResponseDTO
import com.example.musicapplicationse114.model.UserLoginRequest
import com.example.musicapplicationse114.model.UserSignUpRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {
//    suspend fun login1(username: String, password:String):Boolean
//    suspend fun signUp1(username: String, email: String, password: String, confirmPassword: String):Boolean
//    suspend fun loadAlbums():ArrayList<Album>
//    suspend fun loadSong():ArrayList<Song>
//    suspend fun loadRecentPlayed():ArrayList<RecentlyPlayed>


    //auth
    @POST("/login")
    suspend fun login(@Body request : UserLoginRequest): Response<AuthenticationResponse>

    @POST("/register")
    suspend fun register(@Body request: UserSignUpRequest): Response<AuthenticationResponse>


    //album
    @GET("api/albums")
    suspend fun getAlbums(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): AlbumPageResponse

    @GET("api/albums/{id}")
    suspend fun getAlbumById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): AlbumResponse

    @GET("/api/albums/search")
    suspend fun searchAlbums(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): AlbumPageResponse

    //song
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
        ) : SongResponseDTO

    @GET("api/songs/album/{albumId}")
    suspend fun getSongsByAlbumId(
        @Header("Authorization") token: String,
        @Path("albumId") albumId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): SongPageResponse

    @GET("api/songs/genre/{genreId}")
    suspend fun getSongByGenreId(
        @Header("Authorization") token: String,
        @Path("genreId") genreId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): SongPageResponse

    @GET("/api/songs/search")
    suspend fun searchSongs(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<SongPageResponse>


    //favorite song
    @POST("api/favorite-songs")
    suspend fun addFavoriteSong(
        @Header("Authorization") token: String,
        @Body request: AddFavoriteSongRequest
    ) : Response<FavoriteSongResponse>

    @GET("api/favorite-songs")
    suspend fun getFavoriteSongs(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "addedAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<FavoriteSongPageResponse>

    @GET("api/favorite-songs/search")
    suspend fun searchFavoriteSongs(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "addedAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<FavoriteSongPageResponse>

    @DELETE("api/favorite-songs")
    suspend fun removeFavoriteSong(
        @Header("Authorization") token: String,
        @Query("songId") songId: Long
    ): Response<Void>


    //download song
    @POST("api/downloaded-songs")
    suspend fun addDownloadedSong(
        @Header("Authorization") token: String,
        @Query("songId") songId: Long
    ): Response<DownloadedSongResponse>

    @GET("api/downloaded-songs")
    suspend fun getDownloadedSongs(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<DownloadedSongPageResponse>

    @DELETE("api/downloaded-songs")
    suspend fun removeDownloadedSong(
        @Header("Authorization") token: String,
        @Query("songId") songId: Long
    ): Response<Void>


    //artist
    @GET("api/artists")
    suspend fun getArtists(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ArtistPageResponse

    @GET("api/songs/artist/{artistId}")
    suspend fun getSongsByArtistId(
        @Header("Authorization") token: String,
        @Path("artistId") artistId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): SongPageResponse

    @GET("api/artists/{id}")
    suspend fun getArtistById(
        @Header("Authorization") token: String,
        @Path("id") artistId: Long
    ): Response<ArtistResponse>

    @GET("/api/artists/search")
    suspend fun searchArtists(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ArtistPageResponse

    //Genre
    @GET("/api/genres")
    suspend fun getGenres(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<GenrePageResponse>

    //search global
    @GET("/api/search/global")
    suspend fun globalSearch(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ): GlobalSearchResultDTO

    //recently-played
    @POST("/recently-played/me")
    suspend fun addRecentlyPlayed(
        @Header("Authorization") token: String,
        @Query("songId") songId: Long
    ): Response<Unit>

    @GET("/recently-played/me")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "playedAt,desc"
    ): Response<RecentlyPlayedPageResponse<SongResponse>>

    //Follow artist
    @POST("api/follow-artists")
    suspend fun followArtist(
        @Header("Authorization") token: String,
        @Body request: FollowArtistRequest
    ): Response<FollowArtistResponse>

    @DELETE("api/follow-artists")
    suspend fun unfollowArtist(
        @Header("Authorization") token: String,
        @Query("artistId") artistId: Long
    ): Response<Void>

    @GET("api/follow-artists")
    suspend fun getFollowedArtists(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "followedAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<FollowArtistPageResponse>

    @GET("api/follow-artists/search")
    suspend fun searchFollowedArtists(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "followedAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<FollowArtistPageResponse>

    //Playlist
    @POST("/api/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") token: String,
        @Body request: PlaylistRequest
    ): Response<PlaylistResponse>

    // Lấy tất cả playlists (phân trang + sắp xếp)
    @GET("/api/playlists")
    suspend fun getAllPlaylists(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<PlaylistPageResponse<PlaylistResponse>>

    @GET("/api/playlists/my-playlists")
    suspend fun getMyPlaylists(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<PlaylistPageResponse<PlaylistResponse>>


    // Tìm kiếm playlist
    @GET("/api/playlists/search")
    suspend fun searchPlaylists(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<PlaylistPageResponse<PlaylistResponse>>

    @GET("/api/playlists/{playlistId}")
    suspend fun getPlaylistById(
        @Header("Authorization") token: String,
        @Path("playlistId") playlistId: Long
    ): Response<PlaylistResponse>

    @POST("/api/song-playlists")
    suspend fun addSongToPlaylist(
        @Header("Authorization") token: String,
        @Body request: SongPlaylistRequest
    ): Response<SongPlaylist>

    // Get song-playlist relationship by songId and playlistId
    @GET("/api/song-playlists/find")
    suspend fun getSongPlaylistRelation(
        @Header("Authorization") token: String,
        @Query("songId") songId: Long,
        @Query("playlistId") playlistId: Long
    ): Response<SongPlaylist>

    @DELETE("/api/song-playlists/{id}")
    suspend fun deleteSongPlaylistById(
        @Header("Authorization") token: String,
        @Path("id") songPlaylistId: Long
    ): Response<Void>

    @GET("/api/playlists/{playlistId}/with-songs")
    suspend fun getPlaylistWithSongs(
        @Header("Authorization") token: String,
        @Path("playlistId") playlistId: Long
    ): Response<PlaylistResponse>

    // forget password APIs
    @POST("/forget-password/verify-email/{email}")
    @Headers("Accept: text/plain")
    suspend fun verifyEmail(@Path("email") email: String): Response<ResponseBody>

    @POST("/forget-password/verify-otp/{otp}/{email}")
    @Headers("Accept: text/plain")
    suspend fun verifyOtp(@Path("otp") otp: Int, @Path("email") email: String): Response<ResponseBody>

    @POST("/forget-password/change-password/{email}")
    @Headers("Accept: text/plain", "Content-Type: application/json")
    suspend fun changePassword(@Path("email") email: String, @Body request: ChangePasswordRequest): Response<ResponseBody>
  
  //notification
    @GET("/notifications/me")
    suspend fun getMyNotifications(
        @Header("Authorization") token: String
    ): Response<List<NotificationDto>>

    //songChart
    @GET("/api/songs/top")
    suspend fun getTopSongByViewCount(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ) : Response<SongPageResponse>

    //comment endpoints in song controller - require auth headers
    @GET("/api/songs/{songId}/comments")
    suspend fun getCommentsBySongId(
        @Header("Authorization") token: String,
        @Path("songId") songId: Long
    ): Response<List<CommentResponse>>

    @POST("/api/songs/{songId}/comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("songId") songId: Long,
        @Body request: CommentRequest
    ): Response<CommentResponse>

    @DELETE("/api/songs/comments/{commentId}")
    suspend fun deleteComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Long
    ): Response<ResponseBody>

    @POST("/api/songs/{songId}/comments/{commentId}/like")
    suspend fun likeComment(
        @Header("Authorization") token: String,
        @Path("songId") songId: Long,
        @Path("commentId") commentId: Long,
        @Query("userId") userId: Long
    ): Response<CommentActionResponseDTO>

    @DELETE("/api/songs/{songId}/comments/{commentId}/unlike")
    suspend fun unlikeComment(
        @Header("Authorization") token: String,
        @Path("songId") songId: Long,
        @Path("commentId") commentId: Long,
        @Query("userId") userId: Long
    ): Response<CommentActionResponseDTO>

    @GET("/api/comments/{commentId}/like-status")
    suspend fun getLikeStatus(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Long
    ): Response<Boolean>

    // Get user's liked comments for a song
    @GET("/api/songs/{songId}/liked-comments")
    suspend fun getUserLikedComments(
        @Header("Authorization") token: String,
        @Path("songId") songId: Long,
        @Query("userId") userId: Long
    ): Response<List<Long>>

    // Share song endpoint - require auth
    @GET("/api/songs/{songId}/share")
    suspend fun shareSong(
        @Header("Authorization") token: String,
        @Path("songId") songId: Long
    ): Response<ResponseBody>
}