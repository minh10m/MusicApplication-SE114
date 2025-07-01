package com.music.application.be.modules.song;

import com.music.application.be.modules.comment.CommentService;
import com.music.application.be.modules.comment.dto.CommentActionResponseDTO;
import com.music.application.be.modules.comment.dto.CommentResponseDTO;
import com.music.application.be.modules.comment.dto.CreateCommentDTO;
import com.music.application.be.modules.song.dto.CreateSongDTO;
import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.song.dto.SongDTO;
import com.music.application.be.modules.song.dto.SongResponseDTO;
import com.music.application.be.modules.song.dto.UpdateSongDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    @Autowired
    private SongService songService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper; // Inject từ Spring (có sẵn)

    @Operation(
            summary = "Create a new song with audio and thumbnail",
            description = "Creates a new song and uploads audio (MP3/WAV) and thumbnail. Requires multipart/form-data with three parts: 'song' (JSON), 'audio' (file), and 'thumbnail' (file).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully created song",
                            content = @Content(schema = @Schema(implementation = SongDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "415", description = "Unsupported Media Type")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SongDTO> createSong(
            @Parameter(description = "Thông tin bài hát", schema = @Schema(implementation = CreateSongDTO.class))
            @RequestPart("song") String songJson,
            @RequestPart("audio") MultipartFile audioFile,
            @RequestPart("thumbnail") MultipartFile thumbnailFile) throws Exception {
        CreateSongDTO createSongDTO = objectMapper.readValue(songJson, CreateSongDTO.class);
        return ResponseEntity.ok(songService.createSong(createSongDTO, audioFile, thumbnailFile));
    }

    @Operation(
            summary = "Get song by ID",
            description = "Retrieves details of a specific song by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved song",
                            content = @Content(schema = @Schema(implementation = SongDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Song not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<SongResponseDTO> getSongById(@PathVariable Long id) {
        SongResponseDTO songDTO = songService.getSongById(id);
        return ResponseEntity.ok(songDTO);
    }

    @Operation(
            summary = "Get all songs",
            description = "Retrieves a paginated list of all songs.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved songs",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping
    public ResponseEntity<PagedResponse<SongResponseDTO>> getAllSongsByCurrentUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(songService.getAllSongs(pageable));
    }



    @Operation(
            summary = "Update an existing song with optional audio and thumbnail",
            description = "Updates a song's details and optionally uploads new audio (MP3/WAV) and thumbnail. Requires multipart/form-data with parts: 'song' (JSON), 'audio' (file, optional), and 'thumbnail' (file, optional).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated song",
                            content = @Content(schema = @Schema(implementation = SongDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "404", description = "Song not found"),
                    @ApiResponse(responseCode = "415", description = "Unsupported Media Type")
            }
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SongDTO> updateSong(
            @PathVariable Long id,
            @Parameter(description = "Thông tin bài hát", schema = @Schema(implementation = UpdateSongDTO.class))
            @RequestPart("song") String songJson,
            @RequestPart(value = "audio", required = false) MultipartFile audioFile,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile) throws Exception {
        UpdateSongDTO updateSongDTO = objectMapper.readValue(songJson, UpdateSongDTO.class);
        return ResponseEntity.ok(songService.updateSong(id, updateSongDTO, audioFile, thumbnailFile));
    }

    @Operation(
            summary = "Update song thumbnail",
            description = "Updates only the thumbnail of a song by uploading a new image file.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated song thumbnail",
                            content = @Content(schema = @Schema(implementation = SongDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid thumbnail file"),
                    @ApiResponse(responseCode = "404", description = "Song not found")
            }
    )
    @PutMapping(value = "/{id}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SongDTO> updateSongThumbnail(
            @PathVariable Long id,
            @Parameter(description = "Thumbnail image file (JPG, PNG, etc.)")
            @RequestPart("thumbnail") MultipartFile thumbnailFile) throws Exception {
        return ResponseEntity.ok(songService.updateSongThumbnail(id, thumbnailFile));
    }

    @Operation(
            summary = "Delete a song",
            description = "Deletes a song by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully deleted song"),
                    @ApiResponse(responseCode = "404", description = "Song not found")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Search songs",
            description = "Searches for songs by query with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<SongDTO>> searchSongs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(songService.searchSongs(query, page, size));
    }


    @Operation(
            summary = "Get songs by artist",
            description = "Retrieves a paginated list of songs by a specific artist ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved songs",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<PagedResponse<SongDTO>> getSongsByArtist(
            @PathVariable Long artistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(songService.getSongsByArtist(artistId, page, size));
    }

    @Operation(
            summary = "Get songs by genre",
            description = "Retrieves a paginated list of songs by a specific genre ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved songs",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<PagedResponse<SongDTO>> getSongsByGenre(
            @PathVariable Long genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(songService.getSongsByGenre(genreId, page, size));
    }

    @Operation(
            summary = "Get songs by album",
            description = "Retrieves a paginated list of songs by a specific album ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved songs",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "Album not found")
            }
    )
    @GetMapping("/album/{albumId}")
    public ResponseEntity<PagedResponse<SongDTO>> getSongsByAlbumId(
            @PathVariable Long albumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(songService.getSongsByAlbumId(albumId, page, size));
    }

    @Operation(
            summary = "Share song link",
            description = "Generates a shareable link for a song.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully generated share link"),
                    @ApiResponse(responseCode = "404", description = "Song not found")
            }
    )
    @GetMapping("/{id}/share")
    public ResponseEntity<String> shareSong(@PathVariable Long id) {
        return ResponseEntity.ok(songService.shareSong(id));
    }

    @Operation(
            summary = "Create a comment for a song",
            description = "Creates a new comment for a specific song.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully created comment",
                            content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentDTO createCommentDTO) {
        return ResponseEntity.ok(commentService.createComment(id, createCommentDTO));
    }

    @Operation(
            summary = "Like a comment on a song",
            description = "Likes a comment on a specific song.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully liked comment",
                            content = @Content(schema = @Schema(implementation = CommentActionResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Comment or song not found")
            }
    )
    @PostMapping("/{songId}/comments/{commentId}/like")
    public ResponseEntity<CommentActionResponseDTO> likeComment(
            @PathVariable Long songId,
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(commentService.likeComment(commentId, userId));
    }

    @Operation(
            summary = "Unlike a comment on a song",
            description = "Unlikes a comment on a specific song.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully unliked comment",
                            content = @Content(schema = @Schema(implementation = CommentActionResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Comment or song not found")
            }
    )
    @DeleteMapping("/{songId}/comments/{commentId}/unlike")
    public ResponseEntity<CommentActionResponseDTO> unlikeComment(
            @PathVariable Long songId,
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(commentService.unlikeComment(commentId, userId));
    }

    @Operation(
            summary = "Get all comments for a song",
            description = "Retrieves all comments for a specific song.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved comments",
                            content = @Content(schema = @Schema(implementation = List.class)))
            }
    )
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsBySongId(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentsBySongId(id));
    }

    @Operation(
            summary = "Get top songs by view count",
            description = "Retrieves a paginated list of top songs based on view count.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved top songs",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping("/top")
    public ResponseEntity<PagedResponse<SongDTO>> getTopSongsByViewCount(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(songService.getTopSongsByViewCount(page, size));
    }
}