package com.music.application.be.modules.listening_history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
    List<ListeningHistory> findByUserId(Long userId);
}