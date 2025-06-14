package com.music.application.be.modules.favorite_album;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteAlbumRepository extends JpaRepository<FavoriteAlbum, Long> {
    Page<FavoriteAlbum> findByUserId(Long userId, Pageable pageable);
    Page<FavoriteAlbum> findByUserIdAndAlbumNameContainingIgnoreCase(Long userId, String name, Pageable pageable);
    Optional<FavoriteAlbum> findByUserIdAndAlbumId(Long userId, Long albumId);
}