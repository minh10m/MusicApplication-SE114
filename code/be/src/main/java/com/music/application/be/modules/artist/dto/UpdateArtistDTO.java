package com.music.application.be.modules.artist.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateArtistDTO {
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
}