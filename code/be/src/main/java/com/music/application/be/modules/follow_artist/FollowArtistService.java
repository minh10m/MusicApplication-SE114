package com.music.application.be.modules.follow_artist;

import com.music.application.be.modules.artist.Artist;
import com.music.application.be.modules.artist.ArtistRepository;
import com.music.application.be.modules.follow_artist.dto.FollowArtistDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FollowArtistService {

    @Autowired
    private FollowArtistRepository followArtistRepository;

    @Autowired
    private ArtistRepository artistRepository;

    // Follow artist
    @CacheEvict(value = {"followedArtists", "searchedFollowedArtists"}, key = "#userId")
    public FollowArtistDTO followArtist(Long userId, Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + artistId));

        if (followArtistRepository.findByUserIdAndArtistId(userId, artistId).isPresent()) {
            throw new IllegalStateException("User with id " + userId + " has already followed artist with id " + artistId);
        }

        FollowArtist followArtist = new FollowArtist();
        followArtist.setUserId(userId);
        followArtist.setArtist(artist);
        followArtist.setFollowedAt(LocalDateTime.now());

        FollowArtist savedFollow = followArtistRepository.save(followArtist);
        artist.setFollowerCount(artist.getFollowerCount() + 1);
        artistRepository.save(artist);

        return mapToDTO(savedFollow);
    }

    // Unfollow artist
    @CacheEvict(value = {"followedArtists", "searchedFollowedArtists"}, key = "#followArtist.userId")
    public void unfollowArtist(Long id) {
        FollowArtist followArtist = followArtistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Follow relationship not found with id: " + id));
        Artist artist = followArtist.getArtist();

        followArtistRepository.delete(followArtist);
        int newFollowerCount = artist.getFollowerCount() - 1;
        artist.setFollowerCount(Math.max(0, newFollowerCount));
        artistRepository.save(artist);
    }

    // Get followed artists
    public Page<FollowArtistDTO> getFollowedArtists(Long userId, Pageable pageable) {
        return followArtistRepository.findByUserId(userId, pageable).map(this::mapToDTO);
    }

    // Search followed artists
    public Page<FollowArtistDTO> searchFollowedArtists(Long userId, String query, Pageable pageable) {
        return followArtistRepository.findByUserIdAndArtistNameContainingIgnoreCase(userId, query, pageable)
                .map(this::mapToDTO);
    }

    // Map entity to DTO
    private FollowArtistDTO mapToDTO(FollowArtist followArtist) {
        FollowArtistDTO dto = new FollowArtistDTO();
        dto.setId(followArtist.getId());
        dto.setUserId(followArtist.getUserId());
        dto.setArtistId(followArtist.getArtist().getId());
        dto.setFollowedAt(followArtist.getFollowedAt());
        return dto;
    }
}