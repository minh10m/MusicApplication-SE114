package com.music.application.be.modules.album.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "Thông tin bài hát")
@Data
public class CreateAlbumDTO {
    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @NotNull(message = "Release date is mandatory")
    private LocalDate releaseDate;

    @NotBlank(message = "Cover image is mandatory")
    private String coverImage;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotNull(message = "Artist ID is mandatory")
    private Long artistId;
}
