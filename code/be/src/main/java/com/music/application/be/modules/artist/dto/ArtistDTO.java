package com.music.application.be.modules.artist.dto;

import lombok.Data;

@Data
public class ArtistDTO {
    private Long id;
    private String name;
    private String avatar;
    private String description;
    private int followerCount;
}
