package com.music.application.be.modules.favorite_playlist.dto;

import com.music.application.be.modules.playlist.dto.PlaylistDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FavoritePlaylistDTO {
    private Long id;
    private Long userId;
    private PlaylistDTO playlist; // Thay đổi từ playlistId thành PlaylistDTO
    private LocalDateTime addedAt;
}