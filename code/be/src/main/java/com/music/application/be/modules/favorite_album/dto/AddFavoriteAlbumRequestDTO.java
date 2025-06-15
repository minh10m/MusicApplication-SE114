package com.music.application.be.modules.favorite_album.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFavoriteAlbumRequestDTO {
    @NotNull(message = "Album ID is required")
    private Long albumId;
}