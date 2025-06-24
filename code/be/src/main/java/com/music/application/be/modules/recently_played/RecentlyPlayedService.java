package com.music.application.be.modules.recently_played;

import com.music.application.be.modules.recently_played.dto.SongSummaryDto;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
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

        RecentlyPlayed recentlyPlayed = RecentlyPlayed.builder()
                .user(user)
                .song(song)
                .build();

        return recentlyPlayedRepository.save(recentlyPlayed);
    }

    public List<SongSummaryDto> getRecentlyPlayedSongsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return recentlyPlayedRepository.findByUserOrderByPlayedAtDesc(user)
                .stream()
                .map(recentlyPlayed -> {
                    Song song = recentlyPlayed.getSong();
                    return new SongSummaryDto(song.getTitle(), song.getThumbnail());
                })
                .toList();
    }

    @CacheEvict(value = "recentlyPlayed", key = "#user.id")
    public void clearRecentlyPlayed(User user) {
        List<RecentlyPlayed> userHistory = recentlyPlayedRepository.findByUserOrderByPlayedAtDesc(user);
        recentlyPlayedRepository.deleteAll(userHistory);
    }
}
