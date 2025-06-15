package com.music.application.be.modules.favorite_playlist;

import com.music.application.be.modules.favorite_playlist.dto.AddFavoritePlaylistRequestDTO;
import com.music.application.be.modules.favorite_playlist.dto.FavoritePlaylistDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite-playlists")
public class FavoritePlaylistController {

    @Autowired
    private FavoritePlaylistService favoritePlaylistService;

    // Add favorite playlist
    @PostMapping
    public ResponseEntity<FavoritePlaylistDTO> addFavoritePlaylist(
            @Valid @RequestBody AddFavoritePlaylistRequestDTO requestDTO) {
        return ResponseEntity.ok(favoritePlaylistService.addFavoritePlaylist(requestDTO.getPlaylistId()));
    }

    // Get favorite playlists
    @GetMapping
    public ResponseEntity<Page<FavoritePlaylistDTO>> getFavoritePlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(favoritePlaylistService.getFavoritePlaylists(pageable));
    }

    // Search favorite playlists
    @GetMapping("/search")
    public ResponseEntity<Page<FavoritePlaylistDTO>> searchFavoritePlaylists(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(favoritePlaylistService.searchFavoritePlaylists(query, pageable));
    }

    // Remove favorite playlist
    @DeleteMapping
    public ResponseEntity<Void> removeFavoritePlaylist(
            @RequestParam Long playlistId) {
        favoritePlaylistService.removeFavoritePlaylist(playlistId);
        return ResponseEntity.ok().build();
    }
}