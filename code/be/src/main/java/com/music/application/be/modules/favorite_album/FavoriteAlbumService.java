package com.music.application.be.modules.favorite_album;

import com.music.application.be.modules.album.Album;
import com.music.application.be.modules.album.AlbumRepository;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @CachePut(value = "favoriteAlbums", key = "#userId")
    public FavoriteAlbumDTO addFavoriteAlbum(Long userId, Long albumId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        FavoriteAlbum favoriteAlbum = FavoriteAlbum.builder()
                .user(user)
                .album(album)
                .addedAt(LocalDateTime.now())
                .build();

        FavoriteAlbum savedFavorite = favoriteAlbumRepository.save(favoriteAlbum);
        return mapToDTO(savedFavorite);
    }

    // Get favorite albums
    @Cacheable(value = "favoriteAlbums", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<FavoriteAlbumDTO> getFavoriteAlbums(Long userId, Pageable pageable) {
        return favoriteAlbumRepository.findByUserId(userId, pageable).map(this::mapToDTO);
    }

    // Search favorite albums
    @Cacheable(value = "favoriteAlbumsSearch", key = "#userId + '-' + #query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<FavoriteAlbumDTO> searchFavoriteAlbums(Long userId, String query, Pageable pageable) {
        return favoriteAlbumRepository.findByUserIdAndAlbumNameContainingIgnoreCase(userId, query, pageable)
                .map(this::mapToDTO);
    }

    // Remove favorite album
    @CacheEvict(value = "favoriteAlbums", key = "#favoriteAlbum.user.id")
    public void removeFavoriteAlbum(Long id) {
        FavoriteAlbum favoriteAlbum = favoriteAlbumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorite album not found"));
        favoriteAlbumRepository.delete(favoriteAlbum);
    }

    // Map entity to DTO
    private FavoriteAlbumDTO mapToDTO(FavoriteAlbum favoriteAlbum) {
        FavoriteAlbumDTO dto = new FavoriteAlbumDTO();
        dto.setId(favoriteAlbum.getId());
        dto.setUserId(favoriteAlbum.getUser().getId());
        dto.setAlbumId(favoriteAlbum.getAlbum().getId());
        dto.setAddedAt(favoriteAlbum.getAddedAt());
        return dto;
    }
}