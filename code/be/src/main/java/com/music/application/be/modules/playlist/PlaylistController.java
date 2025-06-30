package com.music.application.be.modules.playlist;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.playlist.dto.PlaylistDTO;
import com.music.application.be.modules.playlist.dto.PlaylistRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    // Create playlist for user (no genre required)
    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(@Valid @RequestBody PlaylistRequestDTO playlistRequestDTO) {
        return ResponseEntity.ok(playlistService.createPlaylist(playlistRequestDTO));
    }

    // Create playlist for admin with genres (auto-add songs)
    @PostMapping("/with-genres")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PlaylistDTO> createPlaylistWithGenres(@Valid @RequestBody PlaylistRequestDTO playlistRequestDTO) {
        return ResponseEntity.ok(playlistService.createPlaylistWithGenres(playlistRequestDTO));
    }

    // Get playlist by ID
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDTO> getPlaylistById(@PathVariable Long playlistId) {
        return ResponseEntity.ok(playlistService.getPlaylistById(playlistId));
    }

    // Get playlist with songs by ID
    @GetMapping("/{playlistId}/with-songs")
    public ResponseEntity<PlaylistDTO> getPlaylistWithSongs(@PathVariable Long playlistId) {
        return ResponseEntity.ok(playlistService.getPlaylistWithSongs(playlistId));
    }

    // Get all playlists
    @GetMapping
    public ResponseEntity<PagedResponse<PlaylistDTO>> getAllPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<PlaylistDTO> response = playlistService.getAllPlaylists(pageable);
        return ResponseEntity.ok(response);
    }


    // Update playlist for user (no genre)
    @PutMapping("/{playlistId}")
    public ResponseEntity<PlaylistDTO> updatePlaylist(@PathVariable Long playlistId, @Valid @RequestBody PlaylistRequestDTO playlistRequestDTO) {
        return ResponseEntity.ok(playlistService.updatePlaylist(playlistId, playlistRequestDTO));
    }

    // Update playlist for admin with genres (auto-add songs)
    @PutMapping("/{playlistId}/with-genres")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PlaylistDTO> updatePlaylistWithGenres(@PathVariable Long playlistId, @Valid @RequestBody PlaylistRequestDTO playlistRequestDTO) {
        return ResponseEntity.ok(playlistService.updatePlaylistWithGenres(playlistId, playlistRequestDTO));
    }

    // Delete playlist
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long playlistId) {
        playlistService.deletePlaylist(playlistId);
        return ResponseEntity.ok().build();
    }

    // Search playlists
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<PlaylistDTO>> searchPlaylists(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<PlaylistDTO> response = playlistService.searchPlaylists(query, pageable);
        return ResponseEntity.ok(response);
    }

    // Get playlists by genre
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<PagedResponse<PlaylistDTO>> getPlaylistsByGenre(
            @PathVariable Long genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<PlaylistDTO> pagedResponse = playlistService.getPlaylistsByGenre(genreId, pageable);

        PagedResponse<PlaylistDTO> responseDTO = new PagedResponse<>(
                pagedResponse.getContent(),
                pagedResponse.getPage(),
                pagedResponse.getSize(),
                pagedResponse.getTotalElements(),
                pagedResponse.getTotalPages(),
                pagedResponse.isLast()
        );

        return ResponseEntity.ok(responseDTO);
    }


    // Share playlist
    @GetMapping("/{playlistId}/share")
    public ResponseEntity<String> sharePlaylist(@PathVariable Long playlistId) {
        return ResponseEntity.ok(playlistService.sharePlaylist(playlistId));
    }

    // Get user's own playlists
    @GetMapping("/my-playlists")
    public ResponseEntity<PagedResponse<PlaylistDTO>> getMyPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<PlaylistDTO> pagedResponse = playlistService.getMyPlaylists(pageable);

        // Chuyển từ PagedResponse → PagedResponseDTO nếu hai class khác nhau
        PagedResponse<PlaylistDTO> responseDTO = new PagedResponse<>(
                pagedResponse.getContent(),
                pagedResponse.getPage(),
                pagedResponse.getSize(),
                pagedResponse.getTotalElements(),
                pagedResponse.getTotalPages(),
                pagedResponse.isLast()
        );

        return ResponseEntity.ok(responseDTO);
    }

}