package com.music.application.be.modules.favorite_album;

import com.music.application.be.modules.favorite_album.dto.AddFavoriteAlbumRequestDTO;
import com.music.application.be.modules.favorite_album.dto.FavoriteAlbumDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite-albums")
public class FavoriteAlbumController {

    @Autowired
    private FavoriteAlbumService favoriteAlbumService;

    // Add favorite album
    @PostMapping
    public ResponseEntity<FavoriteAlbumDTO> addFavoriteAlbum(
            @Valid @RequestBody AddFavoriteAlbumRequestDTO requestDTO) {
        return ResponseEntity.ok(favoriteAlbumService.addFavoriteAlbum(requestDTO.getAlbumId()));
    }

    // Get favorite albums
    @GetMapping
    public ResponseEntity<Page<FavoriteAlbumDTO>> getFavoriteAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(favoriteAlbumService.getFavoriteAlbums(pageable));
    }

    // Search favorite albums
    @GetMapping("/search")
    public ResponseEntity<Page<FavoriteAlbumDTO>> searchFavoriteAlbums(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(favoriteAlbumService.searchFavoriteAlbums(query, pageable));
    }

    // Remove favorite album
    @DeleteMapping
    public ResponseEntity<Void> removeFavoriteAlbum(
            @RequestParam Long albumId) {
        favoriteAlbumService.removeFavoriteAlbum(albumId);
        return ResponseEntity.ok().build();
    }
}