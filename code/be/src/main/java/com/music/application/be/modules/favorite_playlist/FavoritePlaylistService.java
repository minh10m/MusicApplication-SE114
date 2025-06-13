package com.music.application.be.modules.favorite_playlist;

import com.music.application.be.modules.favorite_playlist.dto.FavoritePlaylistDTO;
import com.music.application.be.modules.playlist.Playlist;
import com.music.application.be.modules.playlist.PlaylistRepository;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FavoritePlaylistService {

    @Autowired
    private FavoritePlaylistRepository favoritePlaylistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    // Add favorite playlist
    @CachePut(value = "favoritePlaylists", key = "#userId + '-' + #playlistId")
    public FavoritePlaylistDTO addFavoritePlaylist(Long userId, Long playlistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + playlistId));

        FavoritePlaylist favoritePlaylist = FavoritePlaylist.builder()
                .user(user)
                .playlist(playlist)
                .addedAt(LocalDateTime.now())
                .build();

        FavoritePlaylist savedFavorite = favoritePlaylistRepository.save(favoritePlaylist);
        return mapToDTO(savedFavorite);
    }

    // Get favorite playlists
    public Page<FavoritePlaylistDTO> getFavoritePlaylists(Long userId, Pageable pageable) {
        return favoritePlaylistRepository.findByUserId(userId, pageable).map(this::mapToDTO);
    }

    // Search favorite playlists
    public Page<FavoritePlaylistDTO> searchFavoritePlaylists(Long userId, String query, Pageable pageable) {
        return favoritePlaylistRepository.findByUserIdAndPlaylistNameContainingIgnoreCase(userId, query, pageable)
                .map(this::mapToDTO);
    }

    // Remove favorite playlist
    @CacheEvict(value = "favoritePlaylists", allEntries = true)
    public void removeFavoritePlaylist(Long id) {
        FavoritePlaylist favoritePlaylist = favoritePlaylistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Favorite playlist not found with id: " + id));
        favoritePlaylistRepository.delete(favoritePlaylist);
    }

    // Map entity to DTO
    private FavoritePlaylistDTO mapToDTO(FavoritePlaylist favoritePlaylist) {
        FavoritePlaylistDTO dto = new FavoritePlaylistDTO();
        dto.setId(favoritePlaylist.getId());
        dto.setUserId(favoritePlaylist.getUser().getId());
        dto.setPlaylistId(favoritePlaylist.getPlaylist().getId());
        dto.setAddedAt(favoritePlaylist.getAddedAt());
        return dto;
    }
}