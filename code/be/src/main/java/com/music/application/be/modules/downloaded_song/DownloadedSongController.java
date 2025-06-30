package com.music.application.be.modules.downloaded_song;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.downloaded_song.dto.DownloadedSongDTO;
import com.music.application.be.modules.song.dto.SongDTO;
import jakarta.persistence.EntityNotFoundException;
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

    // New endpoint: Get downloaded song by songId - tự động lấy user từ authentication
    @GetMapping("/song/{songId}")
    public ResponseEntity<DownloadedSongDTO> getDownloadedSongBySongId(@PathVariable Long songId) {
        return ResponseEntity.ok(downloadedSongService.getDownloadedSongBySongId(songId));
    }

    @GetMapping("/downloaded/songs")
    public ResponseEntity<?> getDownloadedSongsAsSongDTOs(Pageable pageable) {
        try {
            PagedResponse<SongDTO> songs = downloadedSongService.getDownloadedSongsAsSongDTOs(pageable);
            return ResponseEntity.ok(songs);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(401).body("User not authenticated");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching downloaded songs: " + e.getMessage());
        }
    }


    // Search downloaded songs - tự động lấy user từ authentication
    @GetMapping("/downloaded/songs/search")
    public ResponseEntity<?> searchDownloadedSongs(
            @RequestParam("query") String query,
            Pageable pageable
    ) {
        try {
            PagedResponse<DownloadedSongDTO> result = downloadedSongService.searchDownloadedSongs(query, pageable);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(401).body("User not authenticated");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error searching downloaded songs: " + e.getMessage());
        }
    }


    // Remove downloaded song - chỉ cần songId, userId tự động lấy từ authentication
    @DeleteMapping
    public ResponseEntity<Void> removeDownloadedSong(@RequestParam Long songId) {
        downloadedSongService.removeDownloadedSong(songId);
        return ResponseEntity.ok().build();
    }
}