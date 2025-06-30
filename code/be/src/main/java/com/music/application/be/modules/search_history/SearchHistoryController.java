package com.music.application.be.modules.search_history;

import com.music.application.be.modules.search_history.dto.SearchHistoryDTO;
import com.music.application.be.modules.search_history.dto.SearchHistoryRequest;
import com.music.application.be.modules.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @PostMapping
    public ResponseEntity<?> addSearchHistory(@RequestBody SearchHistoryRequest request) {
        try {
            SearchHistory searchHistory = searchHistoryService.addSearchHistory(request.getUserId(), request.getQuery());
            return ResponseEntity.ok(searchHistory);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding to search history: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getSearchHistory(@PathVariable Long userId) {
        try {
            List<SearchHistoryDTO> searchHistoryList = searchHistoryService.getSearchHistoryByUserId(userId);
            return ResponseEntity.ok(searchHistoryList);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching search history: " + e.getMessage());
        }
    }


    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearSearchHistory(@PathVariable Long userId) {
        try {
            searchHistoryService.clearSearchHistoryByUserId(userId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error clearing search history: " + e.getMessage());
        }
    }
}
