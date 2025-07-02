package com.music.application.be.modules.follow_artist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowArtistRepository extends JpaRepository<FollowArtist, Long> {
    Page<FollowArtist> findByUserId(Long userId, Pageable pageable);
    Page<FollowArtist> findByUserIdAndArtistNameContainingIgnoreCase(Long userId, String name, Pageable pageable);
    Optional<FollowArtist> findByUserIdAndArtistId(Long userId, Long artistId);

    int countByUserId(Long id);
}