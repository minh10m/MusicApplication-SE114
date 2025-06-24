package com.music.application.be.modules.search_history.dto;

import com.music.application.be.modules.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchHistoryRequest {
    private Long userId;
    private String query;
    // getters, setters
}


