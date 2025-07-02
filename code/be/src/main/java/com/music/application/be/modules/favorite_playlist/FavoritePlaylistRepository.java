package com.music.application.be.modules.favorite_playlist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoritePlaylistRepository extends JpaRepository<FavoritePlaylist, Long> {
    Page<FavoritePlaylist> findByUserId(Long userId, Pageable pageable);
    Page<FavoritePlaylist> findByUserIdAndPlaylistNameContainingIgnoreCase(Long userId, String name, Pageable pageable);
    Optional<FavoritePlaylist> findByUserIdAndPlaylistId(Long userId, Long playlistId);
    int countByUserId(Long userId);
}