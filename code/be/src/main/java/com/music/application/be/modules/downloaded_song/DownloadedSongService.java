package com.music.application.be.modules.downloaded_song;

import com.music.application.be.modules.downloaded_song.dto.DownloadedSongDTO;
import com.music.application.be.modules.downloaded_song.dto.DownloadedSongInfoDTO;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.modules.song.dto.SongDTO;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class DownloadedSongService {

    @Autowired
    private DownloadedSongRepository downloadedSongRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;    // Add downloaded song - tự động lấy user từ authentication
    public DownloadedSongDTO addDownloadedSong(Long songId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + songId));

        // Kiểm tra xem quan hệ đã tồn tại chưa
        if (downloadedSongRepository.findByUserIdAndSongId(user.getId(), songId).isPresent()) {
            throw new IllegalStateException("Downloaded song relationship already exists for userId: " + user.getId() + " and songId: " + songId);
        }

        DownloadedSong downloadedSong = DownloadedSong.builder()
                .user(user)
                .song(song)
                .downloadedAt(LocalDateTime.now())
                .build();

        DownloadedSong savedDownload = downloadedSongRepository.save(downloadedSong);
        return mapToDTO(savedDownload);
    }

    public Page<SongDTO> getDownloadedSongsAsSongDTOs(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }

        User user = (User) authentication.getPrincipal();

        return downloadedSongRepository.findByUserId(user.getId(), pageable)
                .map(downloadedSong -> {
                    Song song = downloadedSong.getSong();

                    SongDTO dto = new SongDTO();
                    dto.setId(song.getId());
                    dto.setTitle(song.getTitle());
                    dto.setDuration(song.getDuration());
                    dto.setAudioUrl(song.getAudioUrl());
                    dto.setThumbnail(song.getThumbnail());
                    dto.setLyrics(song.getLyrics());
                    dto.setReleaseDate(song.getReleaseDate());
                    dto.setViewCount(song.getViewCount());

                    if (song.getArtist() != null) {
                        dto.setArtistId(song.getArtist().getId());
                        dto.setArtistName(song.getArtist().getName());
                    }

                    if (song.getAlbum() != null) {
                        dto.setAlbumId(song.getAlbum().getId());
                        dto.setAlbumName(song.getAlbum().getName());
                    }

                    if (song.getGenres() != null) {
                        dto.setGenreIds(song.getGenres().stream()
                                .map(genre -> genre.getId())
                                .collect(Collectors.toList()));
                    }

                    return dto;
                });
    }

    // Get downloaded songs - tự động lấy user từ authentication
    public Page<DownloadedSongDTO> getDownloadedSongs(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return downloadedSongRepository.findByUserId(user.getId(), pageable).map(this::mapToDTO);
    }    // Search downloaded songs - tự động lấy user từ authentication
    public Page<DownloadedSongDTO> searchDownloadedSongs(String query, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return downloadedSongRepository.findByUserIdAndSongTitleContainingIgnoreCase(user.getId(), query, pageable)
                .map(this::mapToDTO);
    }    // Remove downloaded song - tự động lấy user từ authentication
    public void removeDownloadedSong(Long songId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        DownloadedSong downloadedSong = downloadedSongRepository.findByUserIdAndSongId(user.getId(), songId)
                .orElseThrow(() -> new EntityNotFoundException("Downloaded song not found with userId: " + user.getId() + " and songId: " + songId));
        
        downloadedSongRepository.delete(downloadedSong);
    }    // Map entity to DTO - cập nhật để include thông tin song bảo mật hơn
    private DownloadedSongDTO mapToDTO(DownloadedSong downloadedSong) {
        DownloadedSongDTO dto = new DownloadedSongDTO();
        dto.setId(downloadedSong.getId());
        dto.setSong(mapSongToInfoDTO(downloadedSong.getSong())); // Map song entity thành DownloadedSongInfoDTO
        dto.setDownloadedAt(downloadedSong.getDownloadedAt());
        return dto;
    }

    // Helper method để map Song entity thành DownloadedSongInfoDTO (chỉ thông tin cần thiết)
    private DownloadedSongInfoDTO mapSongToInfoDTO(Song song) {
        DownloadedSongInfoDTO songInfoDTO = new DownloadedSongInfoDTO();
        songInfoDTO.setId(song.getId());
        songInfoDTO.setTitle(song.getTitle());
        songInfoDTO.setDuration(song.getDuration());
        songInfoDTO.setThumbnail(song.getThumbnail());
        songInfoDTO.setReleaseDate(song.getReleaseDate());
        songInfoDTO.setArtistName(song.getArtist() != null ? song.getArtist().getName() : null);
        songInfoDTO.setAlbumName(song.getAlbum() != null ? song.getAlbum().getName() : null);
        return songInfoDTO;
    }
}