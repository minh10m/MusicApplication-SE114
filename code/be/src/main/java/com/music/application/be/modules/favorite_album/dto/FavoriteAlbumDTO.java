package com.music.application.be.modules.favorite_album.dto;

import com.music.application.be.modules.album.dto.AlbumDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FavoriteAlbumDTO {
    private Long id;
    private Long userId;
    private AlbumDTO album; // Thay đổi từ albumId thành AlbumDTO
    private LocalDateTime addedAt;
}