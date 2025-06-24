package com.music.application.be.modules.recently_played.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SongSummaryDto {
    private String title;
    private String thumbnail;
    private String artistName;
}
