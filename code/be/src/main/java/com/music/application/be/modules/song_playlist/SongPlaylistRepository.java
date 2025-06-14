package com.music.application.be.modules.song_playlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongPlaylistRepository extends JpaRepository<SongPlaylist, Long> {
    List<SongPlaylist> findByPlaylistIdOrderByAddedAtDesc(Long playlistId);
    List<SongPlaylist> findBySongId(Long songId);
    void deleteByPlaylistId(Long playlistId);
    List<SongPlaylist> findByPlaylistIdOrderByAddedAtAsc(Long playlistId);

    long countByPlaylistId(Long id);
}