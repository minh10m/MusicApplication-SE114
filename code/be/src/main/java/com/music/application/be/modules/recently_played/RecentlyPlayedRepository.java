package com.music.application.be.modules.recently_played;

import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecentlyPlayedRepository extends JpaRepository<RecentlyPlayed, Long> {
    @EntityGraph(attributePaths = {"song", "song.artist"})
    Page<RecentlyPlayed> findByUserOrderByPlayedAtDesc(User user, Pageable pageable);
    Optional<RecentlyPlayed> findByUserAndSong(User user, Song song);

}
