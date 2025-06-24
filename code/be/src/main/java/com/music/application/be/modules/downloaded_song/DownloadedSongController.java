package com.music.application.be.modules.downloaded_song;

import com.music.application.be.modules.downloaded_song.dto.DownloadedSongDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/downloaded-songs")
public class DownloadedSongController {

    @Autowired
    private DownloadedSongService downloadedSongService;

    // Add downloaded song - chỉ cần songId, userId tự động lấy từ authentication
    @PostMapping
    public ResponseEntity<DownloadedSongDTO> addDownloadedSong(@RequestParam Long songId) {
        return ResponseEntity.ok(downloadedSongService.addDownloadedSong(songId));
    }

    // Get downloaded songs - tự động lấy user từ authentication
    @GetMapping
    public ResponseEntity<Page<DownloadedSongDTO>> getDownloadedSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(downloadedSongService.getDownloadedSongs(pageable));
    }

    // Search downloaded songs - tự động lấy user từ authentication
    @GetMapping("/search")
    public ResponseEntity<Page<DownloadedSongDTO>> searchDownloadedSongs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(downloadedSongService.searchDownloadedSongs(query, pageable));
    }

    // Remove downloaded song - chỉ cần songId, userId tự động lấy từ authentication
    @DeleteMapping
    public ResponseEntity<Void> removeDownloadedSong(@RequestParam Long songId) {
        downloadedSongService.removeDownloadedSong(songId);
        return ResponseEntity.ok().build();
    }
}