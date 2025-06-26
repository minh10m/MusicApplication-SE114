package com.music.application.be.modules.downloaded_song.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class  DownloadedSongInfoDTO {
    private Long id;
    private String title;
    private Integer duration;
    private String thumbnail;
    private LocalDate releaseDate;
    private String artistName;
    private String albumName;
}
