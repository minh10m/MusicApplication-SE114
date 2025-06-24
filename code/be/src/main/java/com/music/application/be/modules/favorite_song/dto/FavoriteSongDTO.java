package com.music.application.be.modules.favorite_song.dto;

import com.music.application.be.modules.song.dto.SongDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteSongDTO {
    private Long id;
    private Long userId;
    private SongDTO song; // Thay đổi từ songId thành SongDTO
    private LocalDateTime addedAt;
}