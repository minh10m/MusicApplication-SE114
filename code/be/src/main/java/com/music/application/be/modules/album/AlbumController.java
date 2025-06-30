package com.music.application.be.modules.album;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.album.dto.AlbumResponseDTO;
import com.music.application.be.modules.album.dto.CreateAlbumDTO;
import com.music.application.be.modules.album.dto.UpdateAlbumDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.Collections;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    @Autowired
    private AlbumService albumService;
    @Autowired
    private ObjectMapper objectMapper; // inject từ Spring (có sẵn)

    @Operation(
            summary = "Create a new album with cover image",
            description = "Creates a new album and uploads a cover image. Requires multipart/form-data with two parts: 'album' (JSON) and 'coverImage' (file).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully created album",
                            content = @Content(schema = @Schema(implementation = AlbumResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "415", description = "Unsupported Media Type")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumResponseDTO> createAlbum(
            @Parameter(description = "Thông tin album", schema = @Schema(implementation = CreateAlbumDTO.class))
            @RequestPart("album") String albumJson,
            @RequestPart("coverImage") MultipartFile coverImageFile) throws Exception {

        CreateAlbumDTO createAlbumDTO = objectMapper.readValue(albumJson, CreateAlbumDTO.class);
        AlbumResponseDTO result = albumService.createAlbum(createAlbumDTO, coverImageFile);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get album by ID",
            description = "Retrieves details of a specific album by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved album",
                            content = @Content(schema = @Schema(implementation = AlbumResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Album not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponseDTO> getAlbumById(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.getAlbumById(id));
    }

    @Operation(
            summary = "Get all albums",
            description = "Retrieves a paginated list of all albums.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved albums",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping
    public ResponseEntity<PagedResponse<AlbumResponseDTO>> getAllAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(albumService.getAllAlbums(pageable));
    }

    @Operation(
            summary = "Update an existing album with optional cover image",
            description = "Updates an album's details and optionally uploads a new cover image. Requires multipart/form-data with two parts: 'album' (JSON) and 'coverImage' (file, optional).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated album",
                            content = @Content(schema = @Schema(implementation = AlbumResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "404", description = "Album not found"),
                    @ApiResponse(responseCode = "415", description = "Unsupported Media Type")
            }
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumResponseDTO> updateAlbum(
            @PathVariable Long id,
            @Parameter(description = "Thông tin album", schema = @Schema(implementation = UpdateAlbumDTO.class))
            @RequestPart("album") String albumJson,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImageFile) throws Exception {

        UpdateAlbumDTO updateAlbumDTO = objectMapper.readValue(albumJson, UpdateAlbumDTO.class);
        AlbumResponseDTO result = albumService.updateAlbum(id, updateAlbumDTO, coverImageFile);
        return ResponseEntity.ok(result);
    }

    @PutMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumResponseDTO> updateAlbumCover(
            @PathVariable Long id,
            @RequestPart("coverImage") MultipartFile coverImageFile) throws Exception {

        AlbumResponseDTO result = albumService.updateAlbumCover(id, coverImageFile);
        return ResponseEntity.ok(result);
    }


    @Operation(
            summary = "Delete an album",
            description = "Deletes an album by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Album deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Album not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlbum(@PathVariable Long id) {
        try {
            albumService.deleteAlbum(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body("Album not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting album: " + e.getMessage());
        }
    }


    @Operation(
            summary = "Search albums",
            description = "Searches for albums by query with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<AlbumResponseDTO>> searchAlbums(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(albumService.searchAlbums(query, pageable));
    }

    @Operation(
            summary = "Get albums by artist",
            description = "Retrieves a paginated list of albums by a specific artist ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved albums",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<Page<AlbumResponseDTO>> getAlbumsByArtist(
            @PathVariable Long artistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(albumService.getAlbumsByArtist(artistId, pageable));
    }

    @Operation(
            summary = "Share album link",
            description = "Generates a shareable link for an album.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully generated share link"),
                    @ApiResponse(responseCode = "404", description = "Album not found")
            }
    )
    @GetMapping("/{id}/share")
    public ResponseEntity<String> shareAlbum(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.shareAlbum(id));
    }
}