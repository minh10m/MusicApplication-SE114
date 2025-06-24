package com.music.application.be.modules.favorite_song;

import com.music.application.be.modules.favorite_song.dto.FavoriteSongDTO;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.modules.song.dto.SongDTO;
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
public class FavoriteSongService {

    @Autowired
    private FavoriteSongRepository favoriteSongRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    // Add favorite song
    public FavoriteSongDTO addFavoriteSong(Long songId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + songId));

        // Kiểm tra xem quan hệ đã tồn tại chưa
        if (favoriteSongRepository.findByUserIdAndSongId(user.getId(), songId).isPresent()) {
            throw new IllegalStateException("Favorite song relationship already exists for userId: " + user.getId() + " and songId: " + songId);
        }

        FavoriteSong favoriteSong = FavoriteSong.builder()
                .user(user)
                .song(song)
                .addedAt(LocalDateTime.now())
                .build();

        FavoriteSong savedFavorite = favoriteSongRepository.save(favoriteSong);
        return mapToDTO(savedFavorite);
    }

    // Get favorite songs
    public Page<FavoriteSongDTO> getFavoriteSongs(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return favoriteSongRepository.findByUserId(user.getId(), pageable).map(this::mapToDTO);
    }

    // Search favorite songs
    public Page<FavoriteSongDTO> searchFavoriteSongs(String query, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return favoriteSongRepository.findByUserIdAndSongTitleContainingIgnoreCase(user.getId(), query, pageable)
                .map(this::mapToDTO);
    }

    // Remove favorite song
    public void removeFavoriteSong(Long songId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        FavoriteSong favoriteSong = favoriteSongRepository.findByUserIdAndSongId(user.getId(), songId)
                .orElseThrow(() -> new EntityNotFoundException("Favorite song not found with userId: " + user.getId() + " and songId: " + songId));

        favoriteSongRepository.delete(favoriteSong);
    }    // Map entity to DTO - cập nhật để include thông tin song đầy đủ
    private FavoriteSongDTO mapToDTO(FavoriteSong favoriteSong) {
        FavoriteSongDTO dto = new FavoriteSongDTO();
        dto.setId(favoriteSong.getId());
        dto.setUserId(favoriteSong.getUser().getId());
        dto.setSong(mapSongToDTO(favoriteSong.getSong())); // Map song entity thành SongDTO
        dto.setAddedAt(favoriteSong.getAddedAt());
        return dto;
    }    // Helper method để map Song entity thành SongDTO
    private SongDTO mapSongToDTO(Song song) {
        SongDTO songDTO = new SongDTO();
        songDTO.setId(song.getId());
        songDTO.setTitle(song.getTitle());
        songDTO.setDuration(song.getDuration());
        songDTO.setAudioUrl(song.getAudioUrl());
        songDTO.setThumbnail(song.getThumbnail());
        songDTO.setLyrics(song.getLyrics());
        songDTO.setReleaseDate(song.getReleaseDate());
        songDTO.setViewCount(song.getViewCount());
        songDTO.setArtistId(song.getArtist() != null ? song.getArtist().getId() : null);
        songDTO.setArtistName(song.getArtist() != null ? song.getArtist().getName() : null); // Thêm tên artist
        songDTO.setAlbumId(song.getAlbum() != null ? song.getAlbum().getId() : null);
        songDTO.setAlbumName(song.getAlbum() != null ? song.getAlbum().getName() : null); // Thêm tên album
        songDTO.setGenreIds(song.getGenres() != null ? song.getGenres().stream().map(genre -> genre.getId()).collect(Collectors.toList()) : null);
        return songDTO;
    }
}