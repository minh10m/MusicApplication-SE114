package com.music.application.be.modules.downloaded_song;

import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
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
public class DownloadedSongService {

    @Autowired
    private DownloadedSongRepository downloadedSongRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    // Add downloaded song
    @CachePut(value = "downloadedSongs", key = "#userId")
    public DownloadedSongDTO addDownloadedSong(Long userId, Long songId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        DownloadedSong downloadedSong = DownloadedSong.builder()
                .user(user)
                .song(song)
                .downloadedAt(LocalDateTime.now())
                .build();

        DownloadedSong savedDownload = downloadedSongRepository.save(downloadedSong);
        return mapToDTO(savedDownload);
    }

    // Get downloaded songs
    public Page<DownloadedSongDTO> getDownloadedSongs(Long userId, Pageable pageable) {
        return downloadedSongRepository.findByUserId(userId, pageable).map(this::mapToDTO);
    }

    // Search downloaded songs
    public Page<DownloadedSongDTO> searchDownloadedSongs(Long userId, String query, Pageable pageable) {
        return downloadedSongRepository.findByUserIdAndSongTitleContainingIgnoreCase(userId, query, pageable)
                .map(this::mapToDTO);
    }

    // Remove downloaded song
    @CacheEvict(value = "downloadedSongs", key = "#downloadedSong.user.id")
    public void removeDownloadedSong(Long id) {
        DownloadedSong downloadedSong = downloadedSongRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Downloaded song not found"));
        downloadedSongRepository.delete(downloadedSong);
    }

    // Map entity to DTO
    private DownloadedSongDTO mapToDTO(DownloadedSong downloadedSong) {
        DownloadedSongDTO dto = new DownloadedSongDTO();
        dto.setId(downloadedSong.getId());
        dto.setUserId(downloadedSong.getUser().getId());
        dto.setSongId(downloadedSong.getSong().getId());
        dto.setDownloadedAt(downloadedSong.getDownloadedAt());
        return dto;
    }
}