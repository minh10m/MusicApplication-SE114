package com.music.application.be.modules.playlist;

import com.music.application.be.modules.genre.Genre;
import com.music.application.be.modules.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Page<Playlist> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Playlist p JOIN p.genres g WHERE g IN :genres")
    List<Playlist> findByGenresIn(List<Genre> genres);

    // Thêm method để tìm playlist theo user
    Page<Playlist> findByCreatedBy(User user, Pageable pageable);

    // Tìm playlist public hoặc của user hiện tại
    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true OR p.createdBy = :user")
    Page<Playlist> findByIsPublicTrueOrCreatedBy(User user, Pageable pageable);

    // Tìm playlist public theo tên
    Page<Playlist> findByNameContainingIgnoreCaseAndIsPublicTrue(String name, Pageable pageable);

    // Tìm tất cả playlist public
    Page<Playlist> findByIsPublicTrue(Pageable pageable);    // Tìm playlist theo tên và (public hoặc của user)
    @Query("SELECT p FROM Playlist p WHERE p.name LIKE %:name% AND (p.isPublic = true OR p.createdBy = :user)")
    Page<Playlist> findByNameContainingIgnoreCaseAndIsPublicTrueOrCreatedBy(String name, User user, Pageable pageable);

    // Tìm playlist theo genre (playlist có genre luôn là public)
    @Query("SELECT p FROM Playlist p JOIN p.genres g WHERE g.id = :genreId")
    Page<Playlist> findByGenresId(Long genreId, Pageable pageable);

}