package com.music.application.be.modules.song;

import com.music.application.be.modules.genre.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    Page<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Song> findByArtistId(Long artistId, Pageable pageable);
    Page<Song> findByGenresId(Long genreId, Pageable pageable);

    @Query("SELECT s FROM Song s JOIN s.genres g WHERE g IN :genres")
    List<Song> findByGenresIn(List<Genre> genres);
    Page<Song> findAllByOrderByViewCountDesc(Pageable pageable);
    Page<Song> findByAlbumId(Long albumId, Pageable pageable);
}