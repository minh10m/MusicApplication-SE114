package com.music.application.be.modules.follow_artist;

import com.music.application.be.modules.follow_artist.dto.FollowArtistDTO;
import com.music.application.be.modules.follow_artist.dto.FollowArtistRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow-artists")
public class FollowArtistController {

    @Autowired
    private FollowArtistService followArtistService;

    // Follow artist
    @PostMapping
    public ResponseEntity<FollowArtistDTO> followArtist(
            @Valid @RequestBody FollowArtistRequestDTO requestDTO) {
        return ResponseEntity.ok(followArtistService.followArtist(requestDTO.getArtistId()));
    }

    // Unfollow artist
    @DeleteMapping
    public ResponseEntity<Void> unfollowArtist(
            @RequestParam Long artistId) {
        followArtistService.unfollowArtist(artistId);
        return ResponseEntity.ok().build();
    }

    // Get followed artists with sorting
    @GetMapping
    public ResponseEntity<Page<FollowArtistDTO>> getFollowedArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "followedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(followArtistService.getFollowedArtists(pageable));
    }

    // Search followed artists with sorting
    @GetMapping("/search")
    public ResponseEntity<Page<FollowArtistDTO>> searchFollowedArtists(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "followedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Order.asc(sortBy) : Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(followArtistService.searchFollowedArtists(query, pageable));
    }
}