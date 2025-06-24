package com.music.application.be.modules.search_history;

import com.music.application.be.modules.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findByUser(User user);

    void deleteByUser(User user);
}



