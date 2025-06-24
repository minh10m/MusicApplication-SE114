package com.music.application.be.modules.follow_artist.dto;

import com.music.application.be.modules.artist.dto.ArtistDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowArtistDTO {
    private Long id;
    private Long userId;
    private ArtistDTO artist; // Thay đổi từ artistId thành ArtistDTO
    private LocalDateTime followedAt;
}