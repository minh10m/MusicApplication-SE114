package com.music.application.be.modules.follow_artist;

import com.music.application.be.modules.artist.Artist;
import com.music.application.be.modules.artist.ArtistRepository;
import com.music.application.be.modules.artist.dto.ArtistDTO;
import com.music.application.be.modules.follow_artist.dto.FollowArtistDTO;
import com.music.application.be.modules.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FollowArtistService {

    @Autowired
    private FollowArtistRepository followArtistRepository;

    @Autowired
    private ArtistRepository artistRepository;

    // Follow artist
    public FollowArtistDTO followArtist(Long artistId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + artistId));

        // Kiểm tra xem quan hệ đã tồn tại chưa (đã có sẵn trong code gốc)
        if (followArtistRepository.findByUserIdAndArtistId(user.getId(), artistId).isPresent()) {
            throw new IllegalStateException("User with id " + user.getId() + " has already followed artist with id " + artistId);
        }

        FollowArtist followArtist = new FollowArtist();
        followArtist.setUserId(user.getId());
        followArtist.setArtist(artist);
        followArtist.setFollowedAt(LocalDateTime.now());

        FollowArtist savedFollow = followArtistRepository.save(followArtist);
        artist.setFollowerCount(artist.getFollowerCount() + 1);
        artistRepository.save(artist);

        return mapToDTO(savedFollow);
    }

    // Unfollow artist
    public void unfollowArtist(Long artistId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        FollowArtist followArtist = followArtistRepository.findByUserIdAndArtistId(user.getId(), artistId)
                .orElseThrow(() -> new EntityNotFoundException("Follow relationship not found with userId: " + user.getId() + " and artistId: " + artistId));
        Artist artist = followArtist.getArtist();

        followArtistRepository.delete(followArtist);
        int newFollowerCount = artist.getFollowerCount() - 1;
        artist.setFollowerCount(Math.max(0, newFollowerCount));
        artistRepository.save(artist);
    }

    // Get followed artists
    public Page<FollowArtistDTO> getFollowedArtists(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return followArtistRepository.findByUserId(user.getId(), pageable).map(this::mapToDTO);
    }

    // Search followed artists
    public Page<FollowArtistDTO> searchFollowedArtists(String query, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return followArtistRepository.findByUserIdAndArtistNameContainingIgnoreCase(user.getId(), query, pageable)
                .map(this::mapToDTO);
    }    // Map entity to DTO - cập nhật để include thông tin artist đầy đủ
    private FollowArtistDTO mapToDTO(FollowArtist followArtist) {
        FollowArtistDTO dto = new FollowArtistDTO();
        dto.setId(followArtist.getId());
        dto.setUserId(followArtist.getUserId());
        dto.setArtist(mapArtistToDTO(followArtist.getArtist())); // Map artist entity thành ArtistDTO
        dto.setFollowedAt(followArtist.getFollowedAt());
        return dto;
    }

    // Helper method để map Artist entity thành ArtistDTO
    private ArtistDTO mapArtistToDTO(Artist artist) {
        ArtistDTO artistDTO = new ArtistDTO();
        artistDTO.setId(artist.getId());
        artistDTO.setName(artist.getName());
        artistDTO.setAvatar(artist.getAvatar());
        artistDTO.setDescription(artist.getDescription());
        artistDTO.setFollowerCount(artist.getFollowerCount());
        return artistDTO;
    }
}