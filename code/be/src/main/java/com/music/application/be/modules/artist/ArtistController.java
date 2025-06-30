package com.music.application.be.modules.artist;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.artist.dto.ArtistResponseDTO;
import com.music.application.be.modules.artist.dto.CreateArtistDTO;
import com.music.application.be.modules.artist.dto.UpdateArtistDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private ObjectMapper objectMapper; // Inject từ Spring

    @Operation(
            summary = "Create a new artist with avatar",
            description = "Creates a new artist and uploads an avatar. Requires multipart/form-data with two parts: 'artist' (JSON) and 'avatar' (file).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully created artist",
                            content = @Content(schema = @Schema(implementation = ArtistResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "415", description = "Unsupported Media Type")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')") // Chỉ admin có thể tạo artist
    public ResponseEntity<ArtistResponseDTO> createArtist(
            @Parameter(description = "Thông tin artist", schema = @Schema(implementation = CreateArtistDTO.class))
            @RequestPart("artist") String artistJson,
            @RequestPart("avatar") MultipartFile avatarFile) throws Exception {
        CreateArtistDTO createArtistDTO = objectMapper.readValue(artistJson, CreateArtistDTO.class);
        return ResponseEntity.ok(artistService.createArtist(createArtistDTO, avatarFile));
    }

    @Operation(
            summary = "Get artist by ID",
            description = "Retrieves details of a specific artist by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved artist",
                            content = @Content(schema = @Schema(implementation = ArtistResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Artist not found")
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')") // Admin hoặc user đều xem được
    public ResponseEntity<ArtistResponseDTO> getArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtistById(id));
    }

    @Operation(
            summary = "Get all artists",
            description = "Retrieves a paginated list of all artists.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved artists",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')") // Admin hoặc user đều xem được
    public ResponseEntity<PagedResponse<ArtistResponseDTO>> getAllArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(artistService.getAllArtists(pageable));
    }

    @Operation(
            summary = "Update an existing artist with optional avatar",
            description = "Updates an artist's details and optionally uploads a new avatar. Requires multipart/form-data with two parts: 'artist' (JSON) and 'avatar' (file, optional).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated artist",
                            content = @Content(schema = @Schema(implementation = ArtistResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Artist not found"),
                    @ApiResponse(responseCode = "415", description = "Unsupported Media Type")
            }
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')") // Chỉ admin có thể cập nhật artist
    public ResponseEntity<ArtistResponseDTO> updateArtist(
            @PathVariable Long id,
            @Parameter(description = "Thông tin artist", schema = @Schema(implementation = UpdateArtistDTO.class))
            @RequestPart("artist") String artistJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) throws Exception {
        UpdateArtistDTO updateArtistDTO = objectMapper.readValue(artistJson, UpdateArtistDTO.class);
        return ResponseEntity.ok(artistService.updateArtist(id, updateArtistDTO, avatarFile));
    }

    @Operation(
            summary = "Delete an artist",
            description = "Deletes an artist by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully deleted artist"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Artist not found")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Chỉ admin có thể xóa artist
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Search artists",
            description = "Searches for artists by query with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')") // Admin hoặc user đều xem được
    public ResponseEntity<Page<ArtistResponseDTO>> searchArtists(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(artistService.searchArtists(query, pageable));
    }

    @Operation(
            summary = "Share artist link",
            description = "Generates a shareable link for an artist.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully generated share link"),
                    @ApiResponse(responseCode = "404", description = "Artist not found")
            }
    )
    @GetMapping("/{id}/share")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')") // Admin hoặc user đều xem được
    public ResponseEntity<String> shareArtist(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.shareArtist(id));
    }
}