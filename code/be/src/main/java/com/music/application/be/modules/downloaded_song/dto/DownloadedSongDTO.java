package com.music.application.be.modules.downloaded_song.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DownloadedSongDTO {
    private Long id;
    private DownloadedSongInfoDTO song; // Thay đổi từ SongDTO thành DownloadedSongInfoDTO để bảo mật hơn
    private LocalDateTime downloadedAt;
}