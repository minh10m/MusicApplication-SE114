package com.music.application.be.modules.downloaded_song.dto;

import com.music.application.be.modules.song.dto.SongDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DownloadedSongDTO {
    private Long id;
    private SongDTO song; // Changed from DownloadedSongInfoDTO to SongDTO
    private LocalDateTime downloadedAt;
}