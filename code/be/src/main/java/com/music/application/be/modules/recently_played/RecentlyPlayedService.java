package com.music.application.be.modules.recently_played;

import com.music.application.be.modules.genre.Genre;
import com.music.application.be.modules.recently_played.dto.SongSummaryDto;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.modules.song.dto.SongDTO;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecentlyPlayedService {

    private final RecentlyPlayedRepository recentlyPlayedRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    public RecentlyPlayed addRecentlyPlayed(Long userId, Long songId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NoSuchElementException("Song not found"));

        // Kiểm tra xem bài hát đã tồn tại trong danh sách chưa
        Optional<RecentlyPlayed> existing = recentlyPlayedRepository.findByUserAndSong(user, song);
        existing.ifPresent(recentlyPlayedRepository::delete); // Nếu có thì xoá bản ghi cũ

        RecentlyPlayed recentlyPlayed = RecentlyPlayed.builder()
                .user(user)
                .song(song)
                .build();

        return recentlyPlayedRepository.save(recentlyPlayed); // Lưu mới
    }

    public List<SongDTO> getRecentlyPlayedSongsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return recentlyPlayedRepository.findByUserOrderByPlayedAtDesc(user)
                .stream()
                .map(recentlyPlayed -> {
                    Song song = recentlyPlayed.getSong();

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
                        List<Long> genreIds = song.getGenres().stream()
                                .map(Genre::getId)
                                .collect(Collectors.toList());
                        dto.setGenreIds(genreIds);
                    }

                    return dto;
                })
                .toList();
    }

    public List<SongDTO> getAllRecentlyPlayed() {
        return recentlyPlayedRepository.findAllByOrderByPlayedAtDesc()
                .stream()
                .map(recentlyPlayed -> {
                    Song song = recentlyPlayed.getSong();

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
                })
                .toList();
    }


    @CacheEvict(value = "recentlyPlayed", key = "#user.id")
    public void clearRecentlyPlayed(User user) {
        List<RecentlyPlayed> userHistory = recentlyPlayedRepository.findByUserOrderByPlayedAtDesc(user);
        recentlyPlayedRepository.deleteAll(userHistory);
    }
}
