package com.music.application.be.modules.search;

import com.music.application.be.modules.search.dto.GlobalSearchResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    // Global search - tìm kiếm tất cả các entity
    @GetMapping("/global")
    public ResponseEntity<GlobalSearchResultDTO> globalSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        if (limit > 50) {
            limit = 50; // Giới hạn tối đa để tránh performance issues
        }
        
        return ResponseEntity.ok(searchService.globalSearch(query, limit));
    }
}
