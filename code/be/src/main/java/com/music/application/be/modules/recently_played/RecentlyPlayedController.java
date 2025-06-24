package com.music.application.be.modules.recently_played;

import com.music.application.be.modules.recently_played.dto.RecentlyPlayedRequest;
import com.music.application.be.modules.recently_played.dto.SongSummaryDto;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/recently-played")
@RequiredArgsConstructor
public class RecentlyPlayedController {

    private final RecentlyPlayedService recentlyPlayedService;

    @PostMapping
    public ResponseEntity<?> addRecentlyPlayed(@RequestBody RecentlyPlayedRequest request) {
        try {
            RecentlyPlayed recentlyPlayed = recentlyPlayedService.addRecentlyPlayed(request.getUserId(), request.getSongId());
            return ResponseEntity.ok(recentlyPlayed);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("User or Song not found.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding to recently played: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getRecentlyPlayed(@PathVariable Long userId) {
        try {
            List<SongSummaryDto> recentlyPlayedSongs = recentlyPlayedService.getRecentlyPlayedSongsByUserId(userId);
            return ResponseEntity.ok(recentlyPlayedSongs);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching recently played: " + e.getMessage());
        }
    }



    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearRecentlyPlayed(@PathVariable User user) {
        try {
            recentlyPlayedService.clearRecentlyPlayed(user);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error clearing recently played: " + e.getMessage());
        }
    }
}
