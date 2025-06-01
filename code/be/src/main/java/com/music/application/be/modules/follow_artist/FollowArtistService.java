package com.music.application.be.modules.follow_artist;

import com.music.application.be.modules.artist.Artist;
import com.music.application.be.modules.artist.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
    @CachePut(value = "followedArtists", key = "#userId")
    public FollowArtistDTO followArtist(Long userId, Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

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
    @CacheEvict(value = "followedArtists", key = "#userId")
    public void unfollowArtist(Long userId, Long artistId) {
        FollowArtist followArtist = followArtistRepository.findByUserIdAndArtistId(userId, artistId)
                .orElseThrow(() -> new RuntimeException("Follow relationship not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        followArtistRepository.delete(followArtist);
        artist.setFollowerCount(artist.getFollowerCount() - 1);
        artistRepository.save(artist);
    }

    // Get followed artists
    @Cacheable(value = "followedArtists", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<FollowArtistDTO> getFollowedArtists(Long userId, Pageable pageable) {
        return followArtistRepository.findByUserId(userId, pageable).map(this::mapToDTO);
    }

    // Search followed artists
    @Cacheable(value = "followedArtistsSearch", key = "#userId + '-' + #query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
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