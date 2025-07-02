package com.music.application.be.modules.recently_played;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.recently_played.dto.RecentlyPlayedDTO;
import com.music.application.be.modules.song.dto.SongDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/recently-played")
@RequiredArgsConstructor
public class RecentlyPlayedController {

    private final RecentlyPlayedService recentlyPlayedService;

    @PostMapping("/me")
    public ResponseEntity<?> addRecentlyPlayed(@RequestParam Long songId) {
        try {
            RecentlyPlayedDTO recentlyPlayedDTO = recentlyPlayedService.addRecentlyPlayedForCurrentUser(songId);
            return ResponseEntity.ok(recentlyPlayedDTO);
        } catch (EntityNotFoundException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding recently played: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getRecentlyPlayedByCurrentUser(
            @PageableDefault(size = 20, sort = "playedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            PagedResponse<SongDTO> response = recentlyPlayedService.getRecentlyPlayedByUser(pageable);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching recently played songs: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearRecentlyPlayed() {
        try {
            recentlyPlayedService.clearRecentlyPlayedOfCurrentUser();
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error clearing recently played: " + e.getMessage());
        }
    }
}