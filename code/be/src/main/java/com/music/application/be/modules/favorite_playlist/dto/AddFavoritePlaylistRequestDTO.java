package com.music.application.be.modules.favorite_playlist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFavoritePlaylistRequestDTO {
    @NotNull(message = "Playlist ID is required")
    private Long playlistId;
}