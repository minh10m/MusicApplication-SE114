package com.music.application.be.modules.downloaded_song;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DownloadedSongRepository extends JpaRepository<DownloadedSong, Long> {
    Page<DownloadedSong> findByUserId(Long userId, Pageable pageable);
    Page<DownloadedSong> findByUserIdAndSongTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);
    
    // Thêm method để tìm downloaded song theo userId và songId
    Optional<DownloadedSong> findByUserIdAndSongId(Long userId, Long songId);
    boolean existsByUser_IdAndSong_Id(Long userId, Long songId);
}