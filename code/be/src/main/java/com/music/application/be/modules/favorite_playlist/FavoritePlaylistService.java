package com.music.application.be.modules.favorite_playlist;

import com.music.application.be.modules.favorite_playlist.dto.FavoritePlaylistDTO;
import com.music.application.be.modules.playlist.Playlist;
import com.music.application.be.modules.playlist.PlaylistRepository;
import com.music.application.be.modules.playlist.dto.PlaylistDTO;
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
import java.util.stream.Collectors;

@Service
public class FavoritePlaylistService {

    @Autowired
    private FavoritePlaylistRepository favoritePlaylistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    // Add favorite playlist
    public FavoritePlaylistDTO addFavoritePlaylist(Long playlistId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + playlistId));

        // Kiểm tra xem quan hệ đã tồn tại chưa
        if (favoritePlaylistRepository.findByUserIdAndPlaylistId(user.getId(), playlistId).isPresent()) {
            throw new IllegalStateException("Favorite playlist relationship already exists for userId: " + user.getId() + " and playlistId: " + playlistId);
        }

        FavoritePlaylist favoritePlaylist = FavoritePlaylist.builder()
                .user(user)
                .playlist(playlist)
                .addedAt(LocalDateTime.now())
                .build();

        FavoritePlaylist savedFavorite = favoritePlaylistRepository.save(favoritePlaylist);
        return mapToDTO(savedFavorite);
    }

    // Get favorite playlists
    public Page<FavoritePlaylistDTO> getFavoritePlaylists(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return favoritePlaylistRepository.findByUserId(user.getId(), pageable).map(this::mapToDTO);
    }

    // Search favorite playlists
    public Page<FavoritePlaylistDTO> searchFavoritePlaylists(String query, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return favoritePlaylistRepository.findByUserIdAndPlaylistNameContainingIgnoreCase(user.getId(), query, pageable)
                .map(this::mapToDTO);
    }

    // Remove favorite playlist
    public void removeFavoritePlaylist(Long playlistId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        FavoritePlaylist favoritePlaylist = favoritePlaylistRepository.findByUserIdAndPlaylistId(user.getId(), playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Favorite playlist not found with userId: " + user.getId() + " and playlistId: " + playlistId));

        favoritePlaylistRepository.delete(favoritePlaylist);
    }    // Map entity to DTO - cập nhật để include thông tin playlist đầy đủ
    private FavoritePlaylistDTO mapToDTO(FavoritePlaylist favoritePlaylist) {
        FavoritePlaylistDTO dto = new FavoritePlaylistDTO();
        dto.setId(favoritePlaylist.getId());
        dto.setUserId(favoritePlaylist.getUser().getId());
        dto.setPlaylist(mapPlaylistToDTO(favoritePlaylist.getPlaylist())); // Map playlist entity thành PlaylistDTO
        dto.setAddedAt(favoritePlaylist.getAddedAt());
        return dto;
    }    // Helper method để map Playlist entity thành PlaylistDTO
    private PlaylistDTO mapPlaylistToDTO(Playlist playlist) {
        PlaylistDTO playlistDTO = new PlaylistDTO();
        playlistDTO.setId(playlist.getId());
        playlistDTO.setUserId(playlist.getCreatedBy() != null ? playlist.getCreatedBy().getId() : null);
        playlistDTO.setName(playlist.getName());
        playlistDTO.setDescription(playlist.getDescription());
        playlistDTO.setThumbnail(playlist.getThumbnail());
        playlistDTO.setCreatedAt(playlist.getCreatedAt());
        playlistDTO.setGenreIds(playlist.getGenres().stream().map(genre -> genre.getId()).collect(Collectors.toList()));
        return playlistDTO;
    }
}