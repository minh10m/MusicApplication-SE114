package com.music.application.be.modules.favorite_song.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFavoriteSongRequestDTO {
    @NotNull(message = "Song ID is required")
    private Long songId;
}