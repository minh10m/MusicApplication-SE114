package com.music.application.be.modules.search_history;

import com.music.application.be.modules.search_history.dto.SearchHistoryDTO;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    @CacheEvict(value = "searchHistories", key = "'user-' + #userId")
    public SearchHistory addSearchHistory(Long userId, String query) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setQuery(query);
        return searchHistoryRepository.save(history);
    }

    @Cacheable(value = "searchHistories", key = "'user-' + #userId")
    public List<SearchHistoryDTO> getSearchHistoryByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return searchHistoryRepository.findByUser(user)
                .stream()
                .map(sh -> new SearchHistoryDTO(sh.getQuery(), sh.getSearchedAt()))
                .collect(Collectors.toList());
    }


    @CacheEvict(value = "searchHistories", key = "'user-' + #userId")
    public void clearSearchHistoryByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        searchHistoryRepository.deleteByUser(user);
    }
}
