package com.music.application.be.modules.favorite_album;

import com.music.application.be.modules.album.Album;
import com.music.application.be.modules.album.AlbumRepository;
import com.music.application.be.modules.album.dto.AlbumDTO;
import com.music.application.be.modules.favorite_album.dto.FavoriteAlbumDTO;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FavoriteAlbumService {

    @Autowired
    private FavoriteAlbumRepository favoriteAlbumRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlbumRepository albumRepository;

    // Add favorite album
    public FavoriteAlbumDTO addFavoriteAlbum(Long albumId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + albumId));

        // Kiểm tra xem quan hệ đã tồn tại chưa
        if (favoriteAlbumRepository.findByUserIdAndAlbumId(user.getId(), albumId).isPresent()) {
            throw new IllegalStateException("Favorite album relationship already exists for userId: " + user.getId() + " and albumId: " + albumId);
        }

        FavoriteAlbum favoriteAlbum = FavoriteAlbum.builder()
                .user(user)
                .album(album)
                .addedAt(LocalDateTime.now())
                .build();

        FavoriteAlbum savedFavorite = favoriteAlbumRepository.save(favoriteAlbum);
        return mapToDTO(savedFavorite);
    }

    // Get favorite albums
    public Page<FavoriteAlbumDTO> getFavoriteAlbums(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return favoriteAlbumRepository.findByUserId(user.getId(), pageable).map(this::mapToDTO);
    }

    // Search favorite albums
    public Page<FavoriteAlbumDTO> searchFavoriteAlbums(String query, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return favoriteAlbumRepository.findByUserIdAndAlbumNameContainingIgnoreCase(user.getId(), query, pageable)
                .map(this::mapToDTO);
    }

    // Remove favorite album
    public void removeFavoriteAlbum(Long albumId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        FavoriteAlbum favoriteAlbum = favoriteAlbumRepository.findByUserIdAndAlbumId(user.getId(), albumId)
                .orElseThrow(() -> new EntityNotFoundException("Favorite album not found with userId: " + user.getId() + " and albumId: " + albumId));

        favoriteAlbumRepository.delete(favoriteAlbum);
    }    // Map entity to DTO - cập nhật để include thông tin album đầy đủ
    private FavoriteAlbumDTO mapToDTO(FavoriteAlbum favoriteAlbum) {
        FavoriteAlbumDTO dto = new FavoriteAlbumDTO();
        dto.setId(favoriteAlbum.getId());
        dto.setUserId(favoriteAlbum.getUser().getId());
        dto.setAlbum(mapAlbumToDTO(favoriteAlbum.getAlbum())); // Map album entity thành AlbumDTO
        dto.setAddedAt(favoriteAlbum.getAddedAt());
        return dto;
    }

    // Helper method để map Album entity thành AlbumDTO
    private AlbumDTO mapAlbumToDTO(Album album) {
        AlbumDTO albumDTO = new AlbumDTO();
        albumDTO.setId(album.getId());
        albumDTO.setName(album.getName());
        albumDTO.setReleaseDate(album.getReleaseDate());
        albumDTO.setCoverImage(album.getCoverImage());
        albumDTO.setDescription(album.getDescription());
        albumDTO.setArtistId(album.getArtist() != null ? album.getArtist().getId() : null);
        albumDTO.setArtistName(album.getArtist() != null ? album.getArtist().getName() : null);
        return albumDTO;
    }
}