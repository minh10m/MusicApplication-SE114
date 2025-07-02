package com.music.application.be.modules.recently_played.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RecentlyPlayedDTO {
    private Long id;
    private Long songId;
    private String songTitle;
    private Long userId;
    private String username;
    private LocalDateTime playedAt;

}

