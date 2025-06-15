package com.music.application.be.modules.favorite_song;

import com.music.application.be.modules.favorite_song.dto.AddFavoriteSongRequestDTO;
import com.music.application.be.modules.favorite_song.dto.FavoriteSongDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite-songs")
public class FavoriteSongController {

    @Autowired
    private FavoriteSongService favoriteSongService;

    // Add favorite song
    @PostMapping
    public ResponseEntity<FavoriteSongDTO> addFavoriteSong(
            @Valid @RequestBody AddFavoriteSongRequestDTO requestDTO) {
        return ResponseEntity.ok(favoriteSongService.addFavoriteSong(requestDTO.getSongId()));
    }

    // Get favorite songs with sorting
    @GetMapping
    public ResponseEntity<Page<FavoriteSongDTO>> getFavoriteSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(favoriteSongService.getFavoriteSongs(pageable));
    }

    // Search favorite songs with sorting
    @GetMapping("/search")
    public ResponseEntity<Page<FavoriteSongDTO>> searchFavoriteSongs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(favoriteSongService.searchFavoriteSongs(query, pageable));
    }

    // Remove favorite song
    @DeleteMapping
    public ResponseEntity<Void> removeFavoriteSong(
            @RequestParam Long songId) {
        favoriteSongService.removeFavoriteSong(songId);
        return ResponseEntity.ok().build();
    }
}