package com.music.application.be.modules.favorite_song;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteSongRepository extends JpaRepository<FavoriteSong, Long> {
    Page<FavoriteSong> findByUserId(Long userId, Pageable pageable);
    Page<FavoriteSong> findByUserIdAndSongTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);
    Optional<FavoriteSong> findByUserIdAndSongId(Long userId, Long songId);
    boolean existsByUser_IdAndSong_Id(Long userId, Long songId);

    int countByUserId(Long userId);
}