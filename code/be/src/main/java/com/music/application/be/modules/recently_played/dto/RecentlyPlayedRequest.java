package com.music.application.be.modules.recently_played.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecentlyPlayedRequest {
    private Long userId;
    private Long songId;
}


