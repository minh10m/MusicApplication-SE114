package com.music.application.be.modules.recently_played;

import com.music.application.be.modules.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RecentlyPlayedRepository extends JpaRepository<RecentlyPlayed, Long> {
    List<RecentlyPlayed> findByUserOrderByPlayedAtDesc(User user);
}
