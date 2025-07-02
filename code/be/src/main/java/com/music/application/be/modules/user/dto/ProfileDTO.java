package com.music.application.be.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private int favoriteSongCount;
    private int followedArtistCount;
    private int playlistCount;
}

