package com.music.application.be.modules.song_playlist;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.song_playlist.dto.SongPlaylistDTO;
import com.music.application.be.modules.song_playlist.dto.SongPlaylistRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/song-playlists")
public class SongPlaylistController {

    @Autowired
    private SongPlaylistService songPlaylistService;

    // Add song to playlist
    @PostMapping
    public ResponseEntity<SongPlaylistDTO> addSongToPlaylist(@Valid @RequestBody SongPlaylistRequestDTO requestDTO) {
        return ResponseEntity.ok(songPlaylistService.addSongToPlaylist(requestDTO));
    }

    // Update song or playlist in SongPlaylist
    @PutMapping("/{id}")
    public ResponseEntity<SongPlaylistDTO> updateSongPlaylist(
            @PathVariable Long id,
            @Valid @RequestBody SongPlaylistRequestDTO requestDTO) {
        return ResponseEntity.ok(songPlaylistService.updateSongPlaylist(id, requestDTO));
    }

    // Remove song from playlist
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeSongFromPlaylist(@PathVariable Long id) {
        songPlaylistService.removeSongFromPlaylist(id);
        return ResponseEntity.ok().build();
    }

    // Get all song playlists for admin monitoring
    @GetMapping
    public ResponseEntity<PagedResponse<SongPlaylistDTO>> getAllSongPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(songPlaylistService.getAllSongPlaylists(pageable));
    }
}