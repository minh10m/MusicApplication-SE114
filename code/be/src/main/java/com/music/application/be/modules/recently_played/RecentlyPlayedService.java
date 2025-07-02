package com.music.application.be.modules.recently_played;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.common.PaginationUtils;
import com.music.application.be.modules.genre.Genre;
import com.music.application.be.modules.recently_played.dto.RecentlyPlayedDTO;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.modules.song.dto.SongDTO;
import com.music.application.be.modules.user.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecentlyPlayedService {

    private final RecentlyPlayedRepository recentlyPlayedRepository;
    private final SongRepository songRepository;

    /**
     * Add a song to the user's recently played list.
     * If already exists, it will be refreshed to the top (re-added).
     * Also evicts user's cache.
     */
    @CacheEvict(
            value = "recentlyPlayedByUser",
            key = "T(java.util.Objects).hash('user-' + #root.target.getCurrentUserId(), 0, 10)",
            allEntries = true
    )
    public RecentlyPlayedDTO addRecentlyPlayedForCurrentUser(Long songId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new EntityNotFoundException("User not authenticated");
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NoSuchElementException("Song not found"));

        recentlyPlayedRepository.findByUserAndSong(user, song)
                .ifPresent(recentlyPlayedRepository::delete);

        RecentlyPlayed recentlyPlayed = RecentlyPlayed.builder()
                .user(user)
                .song(song)
                .build();

        RecentlyPlayed saved = recentlyPlayedRepository.save(recentlyPlayed);

        return toRecentlyPlayedDTO(saved);
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        return user.getId();
    }


    /**
     * Get recently played songs for a specific user.
     * This method is cached per user.
     */
    @Cacheable(
            value = "recentlyPlayedByUser",
            key = "T(java.util.Objects).hash(#userId, #pageable.pageNumber, #pageable.pageSize)"
    )
    public PagedResponse<SongDTO> getRecentlyPlayedByUser(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new EntityNotFoundException("User not authenticated");
        }

        Page<RecentlyPlayed> page = recentlyPlayedRepository.findByUserOrderByPlayedAtDesc(user, pageable);

        List<SongDTO> songDTOs = page.getContent()
                .stream()
                .map(this::mapToSongDTO)
                .toList();

        return PaginationUtils.buildPagedResponse(songDTOs, page);
    }

    /**
     * Clear recently played history for a user.
     * Also evicts that user's cache.
     */
    @CacheEvict(value = "recentlyPlayedByUser", allEntries = true)
    public void clearRecentlyPlayedOfCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new EntityNotFoundException("User not authenticated");
        }

        Pageable pageable = PageRequest.of(0, 50); // chọn pageSize phù hợp
        Page<RecentlyPlayed> page;

        do {
            page = recentlyPlayedRepository.findByUserOrderByPlayedAtDesc(user, pageable);
            recentlyPlayedRepository.deleteAll(page.getContent());
        } while (!page.isLast());
    }

    /**
     * Utility method to map RecentlyPlayed entity to SongDTO.
     */
    private SongDTO mapToSongDTO(RecentlyPlayed recentlyPlayed) {
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
    }

    public RecentlyPlayedDTO toRecentlyPlayedDTO(RecentlyPlayed recentlyPlayed) {
        return RecentlyPlayedDTO.builder()
                .id(recentlyPlayed.getId())
                .songId(recentlyPlayed.getSong().getId())
                .songTitle(recentlyPlayed.getSong().getTitle())
                .userId(recentlyPlayed.getUser().getId())
                .username(recentlyPlayed.getUser().getUsername())
                .playedAt(recentlyPlayed.getPlayedAt()) // hoặc .getPlayedAt() nếu bạn có field đó
                .build();
    }
}
